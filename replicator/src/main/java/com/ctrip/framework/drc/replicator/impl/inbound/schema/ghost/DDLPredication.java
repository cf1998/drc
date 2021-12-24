package com.ctrip.framework.drc.replicator.impl.inbound.schema.ghost;

/**
 * @Author limingdong
 * @create 2020/2/24
 */
public class DDLPredication {

    private static final String GHOST_CHANGE_LOG = "_ghc";

    private static final String GHOST_TABLE = "_gho";

    private static final String GHOST_DEL = "_del";

    private static final String GHOST_RENAME = "rename /* gh-ost */ table";

    private static final String GHOST_CREATE_TABLE = "create /* gh-ost */ table";  //create ghost table, then execute `alter xxx`

    private static final String GHOST_ALTER_TABLE = "alter /* gh-ost */ table";  //create ghost table, then execute `alter xxx`

    private static final String GHOST_DROP_TABLE = "drop table";

    private static final String GHOST_COMMENT = "/* gh-ost */";

    private static final String GHOST_DROP_COMMENT = "/* generated by server */";

    private static final GhostDdl createGhostDdl = new CreateTableDdl();

    private static final GhostDdl dropGhostDdl = new DropTableDdl();

    private static final GhostDdl alterGhostDdl = new AlterTableDdl();

    private static final GhostDdl renameGhostDdl = new RenameTableDdl();

    private static final GhostDdl predicateDdl = new PredicateGhostDdl();

    private static final GhostDml predicateDml = new PredicateDml();

    public static boolean mayGhostOps(String table) {  //to compare two times
        return table.endsWith(GHOST_CHANGE_LOG) || table.endsWith(GHOST_TABLE)|| table.endsWith(GHOST_DEL);
    }

    public static boolean mayGhostRename(String tableNameOne, String originTableNameTwo) {
        return tableNameOne.endsWith(GHOST_DEL) || originTableNameTwo.endsWith(GHOST_TABLE);
    }

    public static boolean mayGhostDdl(String query) {  //to compare two times
        return predicateDdl.isGhostDdl(query);
    }

    public static boolean isGhostTable(String table) {
        return predicateDml.isGhostDml(table);
    }

    /**
     * save drc_ddl_log_event and apply to in memory database
     * @param query
     * @return
     */
    public static boolean isGhostDropTable(String query) {
        return dropGhostDdl.isGhostDdl(query);
    }

    /**
     * save drc_ddl_log_event and apply to in memory database
     * @param query
     * @return
     */
    public static boolean isGhostCreateTable(String query) {
        return createGhostDdl.isGhostDdl(query);
    }


    /**
     * save drc_ddl_log_event and apply to in memory database
     * @param query
     * @return
     */
    public static boolean isGhostAlterTable(String query) {
        return alterGhostDdl.isGhostDdl(query);
    }

    /**
     * save drc_ddl_log_event and apply to in memory database
     * then parse `table name` and query `column info` to save as drc_table_map_event
     * @param query
     * @return
     */
    public static boolean isGhostRename(String query) {
        return renameGhostDdl.isGhostDdl(query);
    }

    interface GhostDml {
        boolean isGhostDml(String dbAndTable);
    }

    interface GhostDdl {
        boolean isGhostDdl(String query);
    }

    static abstract class AbstractGhostDdl implements GhostDdl {

        @Override
        public boolean isGhostDdl(String query) {
            if (query != null) {
                String newQuery = query.toLowerCase();
                return check(newQuery);
            }

            return false;
        }

        abstract boolean check(String query);
    }

    static class CreateTableDdl extends AbstractGhostDdl implements GhostDdl {

        @Override
        boolean check(String query) {
            return query.contains(GHOST_CREATE_TABLE) && query.contains(GHOST_TABLE);
        }
    }

    static class DropTableDdl extends AbstractGhostDdl implements GhostDdl {

        @Override
        boolean check(String query) {
            return query.contains(GHOST_DROP_TABLE) && (query.contains(GHOST_CHANGE_LOG) || query.contains(GHOST_DEL));
        }
    }

    static class AlterTableDdl extends AbstractGhostDdl implements GhostDdl {

        @Override
        boolean check(String query) {
            return query.contains(GHOST_ALTER_TABLE) && query.contains(GHOST_TABLE);
        }
    }

    static class RenameTableDdl extends AbstractGhostDdl implements GhostDdl {

        @Override
        boolean check(String query) {
            return query.contains(GHOST_RENAME) && query.contains(GHOST_DEL) && query.contains(GHOST_TABLE);
        }
    }

    static class PredicateGhostDdl extends AbstractGhostDdl implements GhostDdl {

        @Override
        boolean check(String query) {
            return query.contains(GHOST_COMMENT) || query.contains(GHOST_DROP_COMMENT);
        }
    }

    static class PredicateDml implements GhostDml {

        @Override
        public boolean isGhostDml(String dbAndTable) {
            if (dbAndTable != null) {
                String newDbAndTable = dbAndTable.toLowerCase().trim();
                if (newDbAndTable.endsWith(GHOST_CHANGE_LOG) || newDbAndTable.endsWith(GHOST_TABLE)) {
                    return true;
                }
            }

            return false;
        }
    }

}
