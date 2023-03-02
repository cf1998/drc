package com.ctrip.framework.drc.console.service.impl;


import com.ctrip.framework.drc.console.config.DefaultConsoleConfig;
import com.ctrip.framework.drc.console.config.DomainConfig;
import com.ctrip.framework.drc.console.dao.MessengerGroupTblDao;
import com.ctrip.framework.drc.console.dao.MessengerTblDao;
import com.ctrip.framework.drc.console.dao.MhaTblDao;
import com.ctrip.framework.drc.console.dao.ResourceTblDao;
import com.ctrip.framework.drc.console.dao.entity.*;
import com.ctrip.framework.drc.console.dto.MhaDto;
import com.ctrip.framework.drc.console.dto.MqConfigDto;
import com.ctrip.framework.drc.console.enums.BooleanEnum;
import com.ctrip.framework.drc.console.service.DataMediaPairService;
import com.ctrip.framework.drc.console.service.DrcBuildService;
import com.ctrip.framework.drc.console.service.MessengerService;
import com.ctrip.framework.drc.console.service.MhaService;
import com.ctrip.framework.drc.console.service.remote.qconfig.QConfigService;
import com.ctrip.framework.drc.console.utils.MySqlUtils.TableSchemaName;
import com.ctrip.framework.drc.console.vo.check.MqConfigCheckVo;
import com.ctrip.framework.drc.console.vo.check.MqConfigConflictTable;
import com.ctrip.framework.drc.console.vo.display.MessengerVo;
import com.ctrip.framework.drc.console.vo.display.MqConfigVo;
import com.ctrip.framework.drc.console.vo.api.MessengerInfo;
import com.ctrip.framework.drc.console.vo.response.QmqApiResponse;
import com.ctrip.framework.drc.console.vo.response.QmqBuEntity;
import com.ctrip.framework.drc.console.vo.response.QmqBuList;
import com.ctrip.framework.drc.core.entity.Messenger;
import com.ctrip.framework.drc.core.http.HttpUtils;
import com.ctrip.framework.drc.core.monitor.reporter.DefaultTransactionMonitorHolder;
import com.ctrip.framework.drc.core.monitor.reporter.TransactionMonitor;
import com.ctrip.framework.drc.core.mq.MessengerProperties;
import com.ctrip.framework.drc.core.mq.MqType;
import com.ctrip.framework.drc.core.server.common.filter.table.aviator.AviatorRegexFilter;
import com.ctrip.xpipe.codec.JsonCodec;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName MessengerServiceImpl
 * @Author haodongPan
 * @Date 2022/10/8 16:56
 * @Version: $
 */
@Service
public class MessengerServiceImpl implements MessengerService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessengerServiceImpl.class);

    @Autowired private DataMediaPairService dataMediaPairService;
    
    @Autowired private DrcBuildService drcBuildService;
    
    @Autowired private QConfigService qConfigService;
    
    private final TransactionMonitor transactionMonitor = DefaultTransactionMonitorHolder.getInstance(); 
    
    @Autowired private MhaService mhaService;
    
    @Autowired private DomainConfig domainConfig;
    
    @Autowired private DefaultConsoleConfig consoleConfig;

    @Autowired private MessengerGroupTblDao messengerGroupTblDao;

    @Autowired private MessengerTblDao messengerTblDao;

    @Autowired private ResourceTblDao resourceTblDao;
    
    @Autowired private MhaTblDao mhaTblDao;
    

    
    @Override
    public List<Messenger> generateMessengers(Long mhaId) throws SQLException {
        List<Messenger> messengers = Lists.newArrayList();
        MessengerGroupTbl messengerGroupTbl = messengerGroupTblDao.queryByMhaId(mhaId, BooleanEnum.FALSE.getCode());
        if (messengerGroupTbl == null) {
            return messengers;
        }
        
        MessengerProperties messengerProperties = dataMediaPairService.generateMessengerProperties(messengerGroupTbl.getId());
        if (CollectionUtils.isEmpty(messengerProperties.getMqConfigs())) {
            logger.info("no mqConfig, should not generate messenger");
            return messengers;
        }
        String propertiesJson = JsonCodec.INSTANCE.encode(messengerProperties);
        
        List<MessengerTbl> messengerTbls = messengerTblDao.queryByGroupId(messengerGroupTbl.getId());
        for (MessengerTbl messengerTbl : messengerTbls) {
            Messenger messenger = new Messenger();
            ResourceTbl resourceTbl = resourceTblDao.queryByPk(messengerTbl.getResourceId());
            messenger.setIp(resourceTbl.getIp());
            messenger.setPort(messengerTbl.getPort());
            messenger.setNameFilter(messengerProperties.getNameFilter()); // for compatible
            messenger.setGtidExecuted(messengerGroupTbl.getGtidExecuted());
            messenger.setProperties(propertiesJson);
            messengers.add(messenger);
        }
        return messengers;
    }

    @Override
    public List<String> getMessengerIps(Long mhaId) throws SQLException {
        List<String> res = Lists.newArrayList();
        MessengerGroupTbl messengerGroupTbl =
            messengerGroupTblDao.queryByMhaId(mhaId, BooleanEnum.FALSE.getCode());
        List<MessengerTbl> messengerTbls = messengerTblDao.queryByGroupId(messengerGroupTbl.getId());
        for (MessengerTbl messengerTbl : messengerTbls) {
            ResourceTbl resourceTbl = resourceTblDao.queryByPk(messengerTbl.getResourceId());
            res.add(resourceTbl.getIp());
        }
        return res;
    }

    @Override
    public List<MqConfigVo> getMqConfigVos(Long messengerGroupId) throws SQLException {
        List<MqConfigVo> vos = Lists.newArrayList();
        List<DataMediaPairTbl> dataMediaPairs = dataMediaPairService.getPairsByMGroupId(messengerGroupId);
        for (DataMediaPairTbl dataMediaPair : dataMediaPairs) {
            vos.add(MqConfigVo.from(dataMediaPair));
        }
        return vos;
    }

    @Override
    public MqConfigCheckVo checkMqConfig(MqConfigDto dto) throws SQLException {
        MqConfigCheckVo mqConfigCheckVo = new MqConfigCheckVo();
        mqConfigCheckVo.setAllowSubmit(true);
        // todo one topic can only bind one dalcluster
        
        // check tag, same topic same tag
        List<DataMediaPairTbl> mqConfigsByTopic = dataMediaPairService.getPairsByTopic(dto.getTopic());
        if (!CollectionUtils.isEmpty(mqConfigsByTopic)) {
            String tagInDb = mqConfigsByTopic.get(0).getTag();
            if (!Objects.equals(tagInDb,dto.getTag())) {
                mqConfigCheckVo.setTag(tagInDb);
                mqConfigCheckVo.setAllowSubmit(false);
            }
        }

        // check table
        List<DataMediaPairTbl> mqConfigs = dataMediaPairService.getPairsByMGroupId(dto.getMessengerGroupId());
        String namespace = dto.getTable().split("\\\\.")[0];
        String name = dto.getTable().split("\\\\.")[1];
        List<TableSchemaName> matchTables = drcBuildService.getMatchTable(namespace, name, dto.getMhaName(), 0);
        if (dto.getId() != 0L) {
            // update remove itself Config first
            mqConfigs = mqConfigs.stream().filter(tbl -> !tbl.getId().equals(dto.getId())).collect(Collectors.toList());
        }
        List<MqConfigConflictTable> conflictTables = Lists.newArrayList();
        for (TableSchemaName table: matchTables) {
            for (DataMediaPairTbl mqConfig : mqConfigs) {
                AviatorRegexFilter filter = new AviatorRegexFilter(mqConfig.getSrcDataMediaName());
                boolean tableEqual = filter.filter(table.getDirectSchemaTableName());
                boolean tagEqual = Objects.equals(dto.getTag(),mqConfig.getTag());
                if (tableEqual && tagEqual) {
                    MqConfigConflictTable vo = new MqConfigConflictTable();
                    vo.setTable(table.getDirectSchemaTableName());
                    vo.setTopic(mqConfig.getDestDataMediaName());
                    vo.setTag(mqConfig.getTag());
                    conflictTables.add(vo);
                    
                }
            }
        }
        
        mqConfigCheckVo.setConflictTables(conflictTables);
        
        if (!CollectionUtils.isEmpty(conflictTables) ) {
            mqConfigCheckVo.setAllowSubmit(false);
        }
        return mqConfigCheckVo;
    }
    
    
    public List<String> getBusFromQmq() throws Exception {
        List<QmqBuEntity> buEntities = getBuEntitiesFromQmq();
        return buEntities.stream().map(buEntity -> buEntity.getEnName().toLowerCase()).collect(Collectors.toList());
    }
    
    private List<QmqBuEntity> getBuEntitiesFromQmq() throws Exception {
        String qmqBuListUrl = domainConfig.getQmqBuListUrl();
        QmqBuList response = HttpUtils.post(qmqBuListUrl, null, QmqBuList.class);
        return response.getData();
    }
    

    @Override
    public String processAddMqConfig(MqConfigDto dto) throws Exception {
        if (MqType.qmq.name().equalsIgnoreCase(dto.getMqType())) {
            if (!initTopic(dto)) {
                throw new IllegalArgumentException("init Topic error");
            }
            if (!initProducer(dto)) {
                throw new IllegalArgumentException("init producer error");
            }
        } else {
            // kafka todo
        }
        addDalClusterMqConfig(dto);
        return dataMediaPairService.addMqConfig(dto);
    }
    


    @Override
    public String processUpdateMqConfig(MqConfigDto dto) throws Exception {
        if (MqType.qmq.name().equalsIgnoreCase(dto.getMqType())) {
            if (!initTopic(dto)) {
                throw new IllegalArgumentException("init Topic error");
            }
            if (!initProducer(dto)) {
                throw new IllegalArgumentException("init producer error");
            }
        } else {
            // kafka todo
        }
        updateDalClusterMqConfig(dto);
        return dataMediaPairService.updateMqConfig(dto);
    }


    @Override
    public String processDeleteMqConfig(String mhaName, Long mqConfigId) throws Exception {
        disableDalClusterMqConfigIfNecessary(mhaName,mqConfigId);
        return dataMediaPairService.deleteMqConfig(mqConfigId);
    }

    @Override
    public List<MessengerVo> getAllMessengerVos() throws SQLException {
        List<MessengerVo> result = Lists.newArrayList();
        MessengerGroupTbl sample = new MessengerGroupTbl();
        sample.setDeleted(BooleanEnum.FALSE.getCode());
        List<MessengerGroupTbl> messengers = messengerGroupTblDao.queryBy(sample);
        for (MessengerGroupTbl messenger : messengers) {
            MhaDto mhaDto = mhaService.queryMhaInfo(messenger.getMhaId());
            MessengerVo messengerVo = new MessengerVo();
            messengerVo.setMhaName(mhaDto.getMhaName());
            messengerVo.setBu(mhaDto.getBuName());
            messengerVo.setMonitorSwitch(mhaDto.getMonitorSwitch());
            result.add(messengerVo);
        }
        return  result;
    }

    @Override
    public String removeMessengerGroup(String mhaName) throws SQLException {
        MhaTbl mhaTbl = mhaTblDao.queryByMhaName(mhaName, BooleanEnum.FALSE.getCode());
        MessengerGroupTbl mGroup = messengerGroupTblDao.queryByMhaId(mhaTbl.getId(), BooleanEnum.FALSE.getCode());
        mGroup.setDeleted(BooleanEnum.TRUE.getCode());
        List<MessengerTbl> messengers = messengerTblDao.queryByGroupId(mGroup.getId());
        for (MessengerTbl m : messengers) {
            m.setDeleted(BooleanEnum.TRUE.getCode());
        }
        messengerGroupTblDao.update(mGroup);
        messengerTblDao.batchUpdate(messengers);
        return "remove success";
    }

    @Override
    public List<MessengerInfo> getAllMessengersInfo() throws SQLException {
        List<MessengerInfo> res = Lists.newArrayList();

        MessengerGroupTbl sample = new MessengerGroupTbl();
        sample.setDeleted(BooleanEnum.FALSE.getCode());
        List<MessengerGroupTbl> mGroups = messengerGroupTblDao.queryBy(sample);
        for (MessengerGroupTbl mGroup : mGroups) {
            List<MessengerTbl> messengers = messengerTblDao.queryByGroupId(mGroup.getId());
            if (CollectionUtils.isEmpty(messengers)) {
                continue;
            }
            MessengerInfo mInfo = new MessengerInfo();
            MhaTbl mhaTbl = mhaTblDao.queryByPk(mGroup.getMhaId());
            mInfo.setMhaName(mhaTbl.getMhaName());
            List<DataMediaPairTbl> dataMediaPairs = dataMediaPairService.getPairsByMGroupId(mGroup.getId());
            if (CollectionUtils.isEmpty(dataMediaPairs)) {
                continue;
            } else {
                List<String> tables = dataMediaPairs.stream().map(DataMediaPairTbl::getSrcDataMediaName)
                        .collect(Collectors.toList());
                mInfo.setNameFilter(StringUtils.join(tables,","));
            }
            res.add(mInfo);
        }
        return res;
    }
    
//    @Override
//    @PossibleRemote(path = "/api/drc/v1/messenger/mqConfig/ddl",forwardType = ForwardTypeEnum.TO_META_DB,httpType = HttpRequestEnum.POST)
//    public void addDalClusterMqConfigByDDL(String fileDc, String mhaName, String schema, String table) throws SQLException{
//        MhaTbl mhaTbl = mhaTblDao.queryByMhaName(mhaName, BooleanEnum.FALSE.getCode());
//        MessengerGroupTbl mGroupTbl = messengerGroupTblDao.queryByMhaId(mhaTbl.getId(),
//                BooleanEnum.FALSE.getCode());
//        if (mGroupTbl == null) {
//            logger.info("[DDL] no need to addDalClusterMqConfig");
//        } else {
//            String newTable = schema+"."+table;
//            List<DataMediaPairTbl> mqConfigs = dataMediaPairService.getPairsByMGroupId(mGroupTbl.getId());
//            for (DataMediaPairTbl mqConfig : mqConfigs) {
//                AviatorRegexFilter filter = new AviatorRegexFilter(mqConfig.getSrcDataMediaName());
//                if (filter.filter(newTable)) {
//                    logger.info("[DDL] addDalClusterMqConfig for dc:{},topic:{},table:{}",
//                            fileDc,mqConfig.getDestDataMediaName(),schema+"."+table);
//                    addDalClusterMqConfig(fileDc,mhaName,schema,table,mqConfig.getDestDataMediaName(),mqConfig.getTag());
//                }
//            }
//        }
//    }
//
//    private void addDalClusterMqConfig(String fileDc, String mhaName, String schema, String table,String topic,String tag)
//            throws Exception {
//        List<TableSchemaName> matchTables = drcBuildService.getMatchTable(schema,table,mhaName,0);
//        transactionMonitor.logTransaction(
//                "QConfig.OpenApi.MqConfig.Generate",
//                topic,
//                () -> qConfigService.addOrUpdateDalClusterMqConfig(
//                        fileDc,topic,schema + "\\." + table,tag,matchTables
//                )
//        );
//    }
    
    
    private void addDalClusterMqConfig(MqConfigDto dto) throws Exception {
        String[] dbAndTable = dto.getTable().split("\\\\.");
        if (dbAndTable.length != 2) {
            throw new IllegalArgumentException("illegal table name" + dto.getTable());
        }
        List<TableSchemaName> matchTables = drcBuildService.getMatchTable(dbAndTable[0],dbAndTable[1],dto.getMhaName(),0);
        String fileDc = mhaService.getDcNameForMha(dto.getMhaName());
        
        transactionMonitor.logTransaction(
                "QConfig.OpenApi.MqConfig.Generate", 
                dto.getTopic(),
                () -> qConfigService.addOrUpdateDalClusterMqConfig(
                        fileDc,dto.getTopic(),dto.getTable(),dto.getTag(),matchTables
                )
        );
    }
    

    private void updateDalClusterMqConfig(MqConfigDto dto) throws Exception {
        // delete + add
        disableDalClusterMqConfigIfNecessary(dto.getMhaName(),dto.getId());
        addDalClusterMqConfig(dto);
    }

    private void disableDalClusterMqConfigIfNecessary(String mhaName,Long mqConfigId) throws Exception {
        List<DataMediaPairTbl> configsByTopic = dataMediaPairService.getPairsByTopic(mqConfigId);
        DataMediaPairTbl configToBeDeleted = configsByTopic.stream().filter(p -> p.getId().equals(mqConfigId))
                .findFirst().orElse(null);
        List<DataMediaPairTbl> otherConfigs = configsByTopic.stream().filter(p -> !p.getId().equals(mqConfigId))
                .collect(Collectors.toList());
        if (configToBeDeleted == null) {
            throw new IllegalArgumentException("illegal mqConfig: " + mqConfigId);
        }

        String[] dbAndTable = configToBeDeleted.getSrcDataMediaName().split("\\\\.");
        if (dbAndTable.length != 2) {
            throw new IllegalArgumentException("illegal table name" + configToBeDeleted.getSrcDataMediaName());
        }
        
        List<TableSchemaName> matchTables = drcBuildService.getMatchTable(dbAndTable[0],dbAndTable[1],mhaName,0);
        String fileDc = mhaService.getDcNameForMha(mhaName);
        
        transactionMonitor.logTransaction(
                "QConfig.OpenApi.MqConfig.Delete", 
                configToBeDeleted.getDestDataMediaName(),
                () -> qConfigService.removeDalClusterMqConfigIfNecessary(
                        fileDc,
                        configToBeDeleted.getDestDataMediaName(),
                        configToBeDeleted.getSrcDataMediaName(),
                        configToBeDeleted.getTag(), 
                        matchTables,
                        otherConfigs.stream().map(DataMediaPairTbl::getSrcDataMediaName).collect(Collectors.toList())
                )
        );
    }
    
    private boolean initTopic(MqConfigDto dto) throws SQLException {
        String dcNameForMha = mhaService.getDcNameForMha(dto.getMhaName());
        if (consoleConfig.getLocalConfigCloudDc().contains(dcNameForMha)) {
            logger.info("[[tag=qmqInit]] localConfigCloudDc init qmq topic:{}",dto.getTopic());
            return true;
        }
        String topicApplicationUrl = domainConfig.getQmqTopicApplicationUrl(dcNameForMha);
        LinkedHashMap<String, String> requestBody = Maps.newLinkedHashMap();
        requestBody.put("subject",dto.getTopic());
        requestBody.put("cluster",dto.isOrder() ? "ordered" : "default");
        requestBody.put("bu",dto.getBu());
        requestBody.put("creator","drc");
        requestBody.put("emailGroup","rdkjdrc@Ctrip.com");
        QmqApiResponse response = HttpUtils.post(topicApplicationUrl, requestBody, QmqApiResponse.class);
        
        if (response.getStatus() == 0) {
            logger.info("[[tag=qmqInit]] init qmq topic success,topic:{}",dto.getTopic());
        } else if (StringUtils.isNotBlank(response.getStatusMsg())
                && response.getStatusMsg().contains("already existed")) {
            logger.info("[[tag=qmqInit]] init qmq topic success,topic:{} already existed",dto.getTopic());
        } else {
            logger.error("[[tag=qmqInit]] init qmq topic fail,MqConfigDto:{}",dto);
            return false;
        }
        return true;
    }
    
    private boolean initProducer(MqConfigDto dto) throws SQLException {
        String dcNameForMha = mhaService.getDcNameForMha(dto.getMhaName());
        if (consoleConfig.getLocalConfigCloudDc().contains(dcNameForMha)) {
            logger.info("[[tag=qmqInit]] localConfigCloudDc init qmq topic:{}",dto.getTopic());
            return true;
        }
        String producerApplicationUrl = domainConfig.getQmqProducerApplicationUrl(dcNameForMha);
        LinkedHashMap<String, Object> requestBody = Maps.newLinkedHashMap();
        requestBody.put("appCode","100023500");
        requestBody.put("subject",dto.getTopic());
        requestBody.put("durable",false);
        requestBody.put("tableStrategy",0);
        requestBody.put("qpsAvg",1000);
        requestBody.put("qpsMax",5000);
        requestBody.put("msgLength",1000);
        requestBody.put("platform",1);
        requestBody.put("creator","drc");
        requestBody.put("remark","binlog_dataChange_message");
        
        QmqApiResponse response = HttpUtils.post(producerApplicationUrl, requestBody, QmqApiResponse.class);
        if (response.getStatus() == 0) {
            logger.info("[[tag=qmqInit]] init qmq producer success,topic:{}",dto.getTopic());
        } else if (StringUtils.isNotBlank(response.getStatusMsg())
                && response.getStatusMsg().contains("already existed")) {
            logger.info("[[tag=qmqInit]] init success,qmq producer already existed,topic:{}",dto.getTopic());
        } else {
            logger.error("[[tag=qmqInit]] init qmq producer fail,MqConfigDto:{}",dto);
            return false;
        }
        return true;
    }

   

}