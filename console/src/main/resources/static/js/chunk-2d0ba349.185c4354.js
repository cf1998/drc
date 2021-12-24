(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2d0ba349"],{"35e6":function(e,t,o){"use strict";o.r(t);var r=function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("base-component",[o("Breadcrumb",{style:{margin:"15px 0 15px 185px",position:"fixed"}},[o("BreadcrumbItem",{attrs:{to:"/home"}},[e._v("首页")]),o("BreadcrumbItem",{attrs:{to:"/deletedDrcClusters"}},[e._v("DRC软删除集群")])],1),o("Content",{staticClass:"content",style:{padding:"10px",background:"#fff",margin:"50px 0 1px 185px",zIndex:"1"}},[o("div",{staticStyle:{padding:"1px 1px"}},[o("Table",{attrs:{stripe:"",columns:e.columns,data:e.dataWithPage,border:"","span-method":e.handleSpan},scopedSlots:e._u([{key:"action",fn:function(t){var r=t.row,a=t.index;return[o("Button",{staticStyle:{"margin-right":"5px"},attrs:{type:"success",size:"small"},on:{click:function(t){return e.checkConfig(r,a)}}},[e._v("查看")]),o("Button",{staticStyle:{"margin-right":"5px"},attrs:{type:"error",size:"small"},on:{click:function(t){return e.previewRecoverConfig(r,a)}}},[e._v("恢复并配置drc")])]}}])}),o("div",{staticStyle:{"text-align":"center",margin:"16px 0"}},[o("Page",{attrs:{transfer:!0,total:e.total,current:e.current,"page-size":e.size,"show-sizer":"","show-elevator":""},on:{"update:current":function(t){e.current=t},"on-page-size-change":e.handleChangeSize}})],1)],1),o("Modal",{attrs:{title:"DRC历史配置",width:"1200px"},model:{value:e.cluster.modal.config,callback:function(t){e.$set(e.cluster.modal,"config",t)},expression:"cluster.modal.config"}},[o("Form",{staticStyle:{width:"100%"}},[o("FormItem",{attrs:{label:"集群配置"}},[o("Input",{attrs:{type:"textarea",autosize:{minRows:1,maxRows:30},readonly:""},model:{value:e.cluster.config,callback:function(t){e.$set(e.cluster,"config",t)},expression:"cluster.config"}})],1)],1)],1),o("Modal",{attrs:{title:"DRC历史配置",width:"1200px"},on:{"on-ok":e.recoverConfig,"on-cancel":e.clearRecoverConfig},model:{value:e.cluster.modal.recover,callback:function(t){e.$set(e.cluster.modal,"recover",t)},expression:"cluster.modal.recover"}},[o("Form",{staticStyle:{width:"100%"}},[o("FormItem",{attrs:{label:"确认恢复双向复制并进入Drc配置吗？",id:"fontsize"}},[o("Input",{attrs:{type:"textarea",autosize:{minRows:1,maxRows:30},readonly:""},model:{value:e.cluster.config,callback:function(t){e.$set(e.cluster,"config",t)},expression:"cluster.config"}})],1)],1)],1)],1)],1)},a=[],s=(o("4160"),o("caad"),o("c975"),o("fb6a"),o("a434"),o("159b"),o("2909")),n={data:function(){var e=this;return{cluster:{config:"",mhaAToBeRecovered:"",mhaBToBeRecovered:"",modal:{config:!1,recover:!1,recover2:!1}},columns:[{title:"序号",width:75,align:"center",render:function(t,o){return t("span",o.index+1+(e.current-1)*e.size)}},{title:"集群A",key:"srcMha"},{title:"集群B",key:"destMha"},{title:"删除状态",key:"deleted",width:150,align:"center",render:function(e,t){var o="volcano",r="已删除";return e("Tag",{props:{color:o}},r)}},{title:"操作",slot:"action",align:"center"}],mhaGroups:[],total:0,current:1,size:10,mergeColData:[]}},computed:{dataWithPage:function(){var e=this.mhaGroups,t=this.mergeColData,o=this.current*this.size-this.size,r=o+this.size;r>=this.total&&(r=this.total);for(var a=o;a<r;a++)t[a]+a>r?e[a].mergeCol=r-a:e[a].mergeCol=t[a];if(o>=this.size)for(var n=o-this.size;n<o;n++)if(t[n]+n>o){e[o].mergeCol=t[n]+n-o;break}return Object(s["a"])(e).slice(o,r)}},methods:{handleSpan:function(e){var t=e.row,o=(e.column,e.rowIndex,e.columnIndex);if(1===o){var r=0===t.mergeCol?0:t.mergeCol,a=0===t.mergeCol?0:1;return[r,a]}},assembleData:function(e){var t=this,o=[];e.forEach((function(e){o.includes(e.srcMha)||o.push(e.srcMha)}));var r=[];o.forEach((function(e){r.push({srcMha:e,num:0})})),e.forEach((function(e){r.forEach((function(t){e.srcMha===t.srcMha&&t.num++}))})),e.forEach((function(e){r.forEach((function(r){e.srcMha===r.srcMha&&(o.includes(e.srcMha)?(e.mergeCol=r.num,t.mergeColData.push(e.mergeCol),o.splice(o.indexOf(r.srcMha),1)):(e.mergeCol=0,t.mergeColData.push(e.mergeCol)))}))}));var a=e;this.mhaGroups=a,console.log("assemble")},getDeletedMhaGroups:function(){var e=this;this.axios.get("/api/drc/v1/meta/orderedDeletedGroups/all").then((function(t){e.mhaGroups=t.data.data,e.total=e.mhaGroups.length,e.assembleData(e.mhaGroups)})),this.total=this.mhaGroups.length,this.assembleData(this.mhaGroups)},handleChangeSize:function(e){this.size=e},checkConfig:function(e,t){var o=this;console.log(e.srcMha),console.log(e.destMha),this.$Spin.show({render:function(e){return e("div",[e("Icon",{class:"demo-spin-icon-load",props:{size:18}}),e("div","请稍等...")])}}),this.axios.get("/api/drc/v1/meta/config/deletedMhas/"+e.srcMha+","+e.destMha).then((function(e){var t=e.data.data;console.log(t),o.cluster.config=t,o.$Spin.hide(),o.cluster.modal.config=!0}))},previewRecoverConfig:function(e,t){var o=this;console.log(e.srcMha),console.log(e.destMha),this.cluster.mhaAToBeRecovered=e.srcMha,this.cluster.mhaBToBeRecovered=e.destMha,this.$Spin.show({render:function(e){return e("div",[e("Icon",{class:"demo-spin-icon-load",props:{size:18}}),e("div","请稍等...")])}}),this.axios.get("/api/drc/v1/meta/config/deletedMhas/"+e.srcMha+","+e.destMha).then((function(e){var t=e.data.data;console.log(t),o.cluster.config=t,o.$Spin.hide(),o.cluster.modal.recover=!0}))},recoverConfig:function(){var e=this;console.log("mhaAToBeRecovered",this.cluster.mhaAToBeRecovered),console.log("mhaBToBeRecovered",this.cluster.mhaBToBeRecovered),this.$Spin.show({render:function(e){return e("div",[e("Icon",{class:"demo-spin-icon-load",props:{size:18}}),e("div","请稍等...进入drc配置选择页面")])}}),this.axios.post("/api/drc/v1/meta/config/recoverMhas/"+this.cluster.mhaAToBeRecovered+","+this.cluster.mhaBToBeRecovered).then((function(t){var o=t.data.data;console.log(o),o&&location.reload(),e.$Spin.hide()})).then(this.goToLink({srcMha:this.cluster.mhaAToBeRecovered,destMha:this.cluster.mhaBToBeRecovered})),this.clearRecoverConfig()},goToLink:function(e){console.log("go to change config for "+e.srcMha+" and "+e.destMha),this.$router.push({path:"/access",query:{step:"3",clustername:e.srcMha,newclustername:e.destMha}})},clearRecoverConfig:function(){console.log("clear mhaAToBeRecovered",this.cluster.mhaAToBeRecovered),console.log("clear mhaBToBeRecovered",this.cluster.mhaBToBeRecovered),this.cluster.mhaAToBeRecovered="",this.cluster.mhaBToBeRecovered=""},moreOperation:function(e){this.$router.push({name:"incrementDataConsistencyCheck",query:{clusterA:e.srcMha,clusterB:e.destMha}})}},created:function(){this.getDeletedMhaGroups()}},c=n,i=o("2877"),l=Object(i["a"])(c,r,a,!1,null,null,null);t["default"]=l.exports}}]);
//# sourceMappingURL=chunk-2d0ba349.185c4354.js.map