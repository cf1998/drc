(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-74447852"],{"057f":function(t,e,r){var n=r("fc6a"),o=r("241c").f,i={}.toString,a="object"==typeof window&&window&&Object.getOwnPropertyNames?Object.getOwnPropertyNames(window):[],s=function(t){try{return o(t)}catch(e){return a.slice()}};t.exports.f=function(t){return a&&"[object Window]"==i.call(t)?s(t):o(n(t))}},"25f0":function(t,e,r){"use strict";var n=r("6eeb"),o=r("825a"),i=r("d039"),a=r("ad6d"),s="toString",c=RegExp.prototype,u=c[s],l=i((function(){return"/a/b"!=u.call({source:"a",flags:"b"})})),d=u.name!=s;(l||d)&&n(RegExp.prototype,s,(function(){var t=o(this),e=String(t.source),r=t.flags,n=String(void 0===r&&t instanceof RegExp&&!("flags"in c)?a.call(t):r);return"/"+e+"/"+n}),{unsafe:!0})},2909:function(t,e,r){"use strict";function n(t){if(Array.isArray(t)){for(var e=0,r=new Array(t.length);e<t.length;e++)r[e]=t[e];return r}}r.d(e,"a",(function(){return a}));r("a4d3"),r("e01a"),r("d28b"),r("a630"),r("e260"),r("d3b7"),r("25f0"),r("3ca3"),r("ddb0");function o(t){if(Symbol.iterator in Object(t)||"[object Arguments]"===Object.prototype.toString.call(t))return Array.from(t)}function i(){throw new TypeError("Invalid attempt to spread non-iterable instance")}function a(t){return n(t)||o(t)||i()}},"3ca3":function(t,e,r){"use strict";var n=r("6547").charAt,o=r("69f3"),i=r("7dd0"),a="String Iterator",s=o.set,c=o.getterFor(a);i(String,"String",(function(t){s(this,{type:a,string:String(t),index:0})}),(function(){var t,e=c(this),r=e.string,o=e.index;return o>=r.length?{value:void 0,done:!0}:(t=n(r,o),e.index+=t.length,{value:t,done:!1})}))},"4df4":function(t,e,r){"use strict";var n=r("0366"),o=r("7b0b"),i=r("9bdd"),a=r("e95a"),s=r("50c4"),c=r("8418"),u=r("35a1");t.exports=function(t){var e,r,l,d,f,g,h=o(t),y="function"==typeof this?this:Array,p=arguments.length,m=p>1?arguments[1]:void 0,v=void 0!==m,b=u(h),x=0;if(v&&(m=n(m,p>2?arguments[2]:void 0,2)),void 0==b||y==Array&&a(b))for(e=s(h.length),r=new y(e);e>x;x++)g=v?m(h[x],x):h[x],c(r,x,g);else for(d=b.call(h),f=d.next,r=new y;!(l=f.call(d)).done;x++)g=v?i(d,m,[l.value,x],!0):l.value,c(r,x,g);return r.length=x,r}},6547:function(t,e,r){var n=r("a691"),o=r("1d80"),i=function(t){return function(e,r){var i,a,s=String(o(e)),c=n(r),u=s.length;return c<0||c>=u?t?"":void 0:(i=s.charCodeAt(c),i<55296||i>56319||c+1===u||(a=s.charCodeAt(c+1))<56320||a>57343?t?s.charAt(c):i:t?s.slice(c,c+2):a-56320+(i-55296<<10)+65536)}};t.exports={codeAt:i(!1),charAt:i(!0)}},"746f":function(t,e,r){var n=r("428f"),o=r("5135"),i=r("e538"),a=r("9bf2").f;t.exports=function(t){var e=n.Symbol||(n.Symbol={});o(e,t)||a(e,t,{value:i.f(t)})}},8418:function(t,e,r){"use strict";var n=r("c04e"),o=r("9bf2"),i=r("5c6c");t.exports=function(t,e,r){var a=n(e);a in t?o.f(t,a,i(0,r)):t[a]=r}},a4d3:function(t,e,r){"use strict";var n=r("23e7"),o=r("da84"),i=r("d066"),a=r("c430"),s=r("83ab"),c=r("4930"),u=r("fdbf"),l=r("d039"),d=r("5135"),f=r("e8b5"),g=r("861d"),h=r("825a"),y=r("7b0b"),p=r("fc6a"),m=r("c04e"),v=r("5c6c"),b=r("7c73"),x=r("df75"),S=r("241c"),N=r("057f"),C=r("7418"),O=r("06cf"),w=r("9bf2"),P=r("d1e7"),D=r("9112"),k=r("6eeb"),L=r("5692"),R=r("f772"),U=r("d012"),T=r("90e3"),A=r("b622"),_=r("e538"),j=r("746f"),z=r("d44e"),M=r("69f3"),E=r("b727").forEach,B=R("hidden"),I="Symbol",$="prototype",V=A("toPrimitive"),G=M.set,F=M.getterFor(I),H=Object[$],J=o.Symbol,q=i("JSON","stringify"),W=O.f,Q=w.f,K=N.f,X=P.f,Y=L("symbols"),Z=L("op-symbols"),tt=L("string-to-symbol-registry"),et=L("symbol-to-string-registry"),rt=L("wks"),nt=o.QObject,ot=!nt||!nt[$]||!nt[$].findChild,it=s&&l((function(){return 7!=b(Q({},"a",{get:function(){return Q(this,"a",{value:7}).a}})).a}))?function(t,e,r){var n=W(H,e);n&&delete H[e],Q(t,e,r),n&&t!==H&&Q(H,e,n)}:Q,at=function(t,e){var r=Y[t]=b(J[$]);return G(r,{type:I,tag:t,description:e}),s||(r.description=e),r},st=u?function(t){return"symbol"==typeof t}:function(t){return Object(t)instanceof J},ct=function(t,e,r){t===H&&ct(Z,e,r),h(t);var n=m(e,!0);return h(r),d(Y,n)?(r.enumerable?(d(t,B)&&t[B][n]&&(t[B][n]=!1),r=b(r,{enumerable:v(0,!1)})):(d(t,B)||Q(t,B,v(1,{})),t[B][n]=!0),it(t,n,r)):Q(t,n,r)},ut=function(t,e){h(t);var r=p(e),n=x(r).concat(ht(r));return E(n,(function(e){s&&!dt.call(r,e)||ct(t,e,r[e])})),t},lt=function(t,e){return void 0===e?b(t):ut(b(t),e)},dt=function(t){var e=m(t,!0),r=X.call(this,e);return!(this===H&&d(Y,e)&&!d(Z,e))&&(!(r||!d(this,e)||!d(Y,e)||d(this,B)&&this[B][e])||r)},ft=function(t,e){var r=p(t),n=m(e,!0);if(r!==H||!d(Y,n)||d(Z,n)){var o=W(r,n);return!o||!d(Y,n)||d(r,B)&&r[B][n]||(o.enumerable=!0),o}},gt=function(t){var e=K(p(t)),r=[];return E(e,(function(t){d(Y,t)||d(U,t)||r.push(t)})),r},ht=function(t){var e=t===H,r=K(e?Z:p(t)),n=[];return E(r,(function(t){!d(Y,t)||e&&!d(H,t)||n.push(Y[t])})),n};if(c||(J=function(){if(this instanceof J)throw TypeError("Symbol is not a constructor");var t=arguments.length&&void 0!==arguments[0]?String(arguments[0]):void 0,e=T(t),r=function(t){this===H&&r.call(Z,t),d(this,B)&&d(this[B],e)&&(this[B][e]=!1),it(this,e,v(1,t))};return s&&ot&&it(H,e,{configurable:!0,set:r}),at(e,t)},k(J[$],"toString",(function(){return F(this).tag})),k(J,"withoutSetter",(function(t){return at(T(t),t)})),P.f=dt,w.f=ct,O.f=ft,S.f=N.f=gt,C.f=ht,_.f=function(t){return at(A(t),t)},s&&(Q(J[$],"description",{configurable:!0,get:function(){return F(this).description}}),a||k(H,"propertyIsEnumerable",dt,{unsafe:!0}))),n({global:!0,wrap:!0,forced:!c,sham:!c},{Symbol:J}),E(x(rt),(function(t){j(t)})),n({target:I,stat:!0,forced:!c},{for:function(t){var e=String(t);if(d(tt,e))return tt[e];var r=J(e);return tt[e]=r,et[r]=e,r},keyFor:function(t){if(!st(t))throw TypeError(t+" is not a symbol");if(d(et,t))return et[t]},useSetter:function(){ot=!0},useSimple:function(){ot=!1}}),n({target:"Object",stat:!0,forced:!c,sham:!s},{create:lt,defineProperty:ct,defineProperties:ut,getOwnPropertyDescriptor:ft}),n({target:"Object",stat:!0,forced:!c},{getOwnPropertyNames:gt,getOwnPropertySymbols:ht}),n({target:"Object",stat:!0,forced:l((function(){C.f(1)}))},{getOwnPropertySymbols:function(t){return C.f(y(t))}}),q){var yt=!c||l((function(){var t=J();return"[null]"!=q([t])||"{}"!=q({a:t})||"{}"!=q(Object(t))}));n({target:"JSON",stat:!0,forced:yt},{stringify:function(t,e,r){var n,o=[t],i=1;while(arguments.length>i)o.push(arguments[i++]);if(n=e,(g(e)||void 0!==t)&&!st(t))return f(e)||(e=function(t,e){if("function"==typeof n&&(e=n.call(this,t,e)),!st(e))return e}),o[1]=e,q.apply(null,o)}})}J[$][V]||D(J[$],V,J[$].valueOf),z(J,I),U[B]=!0},a630:function(t,e,r){var n=r("23e7"),o=r("4df4"),i=r("1c7e"),a=!i((function(t){Array.from(t)}));n({target:"Array",stat:!0,forced:a},{from:o})},ad6d:function(t,e,r){"use strict";var n=r("825a");t.exports=function(){var t=n(this),e="";return t.global&&(e+="g"),t.ignoreCase&&(e+="i"),t.multiline&&(e+="m"),t.dotAll&&(e+="s"),t.unicode&&(e+="u"),t.sticky&&(e+="y"),e}},d28b:function(t,e,r){var n=r("746f");n("iterator")},ddb0:function(t,e,r){var n=r("da84"),o=r("fdbc"),i=r("e260"),a=r("9112"),s=r("b622"),c=s("iterator"),u=s("toStringTag"),l=i.values;for(var d in o){var f=n[d],g=f&&f.prototype;if(g){if(g[c]!==l)try{a(g,c,l)}catch(y){g[c]=l}if(g[u]||a(g,u,d),o[d])for(var h in i)if(g[h]!==i[h])try{a(g,h,i[h])}catch(y){g[h]=i[h]}}}},e01a:function(t,e,r){"use strict";var n=r("23e7"),o=r("83ab"),i=r("da84"),a=r("5135"),s=r("861d"),c=r("9bf2").f,u=r("e893"),l=i.Symbol;if(o&&"function"==typeof l&&(!("description"in l.prototype)||void 0!==l().description)){var d={},f=function(){var t=arguments.length<1||void 0===arguments[0]?void 0:String(arguments[0]),e=this instanceof f?new l(t):void 0===t?l():l(t);return""===t&&(d[e]=!0),e};u(f,l);var g=f.prototype=l.prototype;g.constructor=f;var h=g.toString,y="Symbol(test)"==String(l("test")),p=/^Symbol\((.*)\)[^)]+$/;c(g,"description",{configurable:!0,get:function(){var t=s(this)?this.valueOf():this,e=h.call(t);if(a(d,t))return"";var r=y?e.slice(7,-1):e.replace(p,"$1");return""===r?void 0:r}}),n({global:!0,forced:!0},{Symbol:f})}},e538:function(t,e,r){var n=r("b622");e.f=n},f4c1:function(t,e,r){"use strict";r.r(e);var n=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("base-component",[r("Breadcrumb",{style:{margin:"15px 0 15px 185px",position:"fixed"}},[r("BreadcrumbItem",{attrs:{to:"/home"}},[t._v("首页")]),r("BreadcrumbItem",{attrs:{to:"/proxyRouteCluster"}},[t._v("路由列表")])],1),r("Content",{staticClass:"content",style:{padding:"10px",background:"#fff",margin:"50px 0 1px 185px",zIndex:"1"}},[r("div",{staticStyle:{padding:"1px 1px"}},[r("Card",[t._v(" BU名："),r("Input",{style:{width:"180px",marginRight:"10px"},model:{value:t.searchCondition.routeOrgName,callback:function(e){t.$set(t.searchCondition,"routeOrgName",e)},expression:"searchCondition.routeOrgName"}}),t._v(" 源端机房："),r("Input",{style:{width:"180px",marginRight:"10px"},model:{value:t.searchCondition.srcDcName,callback:function(e){t.$set(t.searchCondition,"srcDcName",e)},expression:"searchCondition.srcDcName"}}),t._v(" 目标机房："),r("Input",{style:{width:"180px",marginRight:"10px"},model:{value:t.searchCondition.dstDcName,callback:function(e){t.$set(t.searchCondition,"dstDcName",e)},expression:"searchCondition.dstDcName"}}),t._v(" tag："),r("Input",{style:{width:"180px",marginRight:"10px"},model:{value:t.searchCondition.tag,callback:function(e){t.$set(t.searchCondition,"tag",e)},expression:"searchCondition.tag"}}),t._v(" deleted："),r("Select",{staticStyle:{width:"200px"},attrs:{placeholder:"inUse"},on:{"on-change":t.getRoutes},model:{value:t.searchCondition.deleted,callback:function(e){t.$set(t.searchCondition,"deleted",e)},expression:"searchCondition.deleted"}},t._l(t.searchOption.deleteOptions,(function(e){return r("Option",{key:e.key,attrs:{value:e.value}},[t._v(t._s(e.key))])})),1),r("Button",{style:{marginLeft:"10px"},attrs:{type:"primary"},on:{click:t.getRoutes}},[t._v("查询")]),r("Button",{style:{marginLeft:"10px"},attrs:{type:"primary"},on:{click:t.goToRouteManagementLink}},[t._v("添加路由")]),r("Table",{attrs:{stripe:"",columns:t.columns,data:t.dataWithPage},scopedSlots:t._u([{key:"action",fn:function(e){var n=e.row,o=e.index;return[0===n.deleted?r("Button",{staticStyle:{"margin-right":"5px"},attrs:{type:"error",size:"small"},on:{click:function(e){return t.goToDelete(n,o)}}},[t._v("下线")]):t._e(),0===n.deleted?r("Button",{staticStyle:{"margin-right":"5px"},attrs:{type:"primary",size:"small"},on:{click:function(e){return t.goToLink(n,o)}}},[t._v("修改")]):t._e(),1===n.deleted?r("Button",{staticStyle:{"margin-right":"5px"},attrs:{type:"warning",size:"small"},on:{click:function(e){return t.goToRecover(n,o)}}},[t._v("回滚")]):t._e()]}}])}),r("div",{staticStyle:{"text-align":"center",margin:"16px 0"}},[r("Page",{attrs:{transfer:!0,total:t.routes.length,current:t.current,"show-sizer":"","show-elevator":""},on:{"update:current":function(e){t.current=e},"on-page-size-change":t.handleChangeSize}})],1)],1)],1)])],1)},o=[],i=(r("fb6a"),r("2909")),a={name:"proxyRouteCluster",data:function(){var t=this;return{columns:[{title:"序号",width:75,align:"center",render:function(e,r){return e("span",r.index+1+(t.current-1)*t.size)}},{title:"BU",key:"routeOrgName"},{title:"源端机房",key:"srcDcName"},{title:"目标机房",key:"dstDcName"},{title:"源Region",key:"srcRegionName"},{title:"目标Region",key:"dstRegionName"},{title:"源端Proxy",key:"srcProxyUris"},{title:"目标Proxy",key:"dstProxyUris"},{title:"中继Proxy",key:"relayProxyUris"},{title:"tag",key:"tag"},{title:"状态",key:"deleted",align:"center",resizable:!0,render:function(t,e){var r=e.row,n=0===r.deleted?"blue":"volcano",o=0===r.deleted?"inUse":"discard";return t("Tag",{props:{color:n}},o)}},{title:"操作",slot:"action",align:"center",width:200}],searchCondition:{routeOrgName:"",srcDcName:"",dstDcName:"",tag:"",deleted:0},routes:[],total:0,current:1,size:10,searchOption:{deleteOptions:[{value:0,key:"inUse"},{value:1,key:"discard"}]}}},computed:{dataWithPage:function(){var t=this.routes,e=this.current*this.size-this.size,r=e+this.size;return Object(i["a"])(t).slice(e,r)}},methods:{getRoutes:function(){var t=this,e="/api/drc/v1/meta/routes",r=!1;""!==this.searchCondition.routeOrgName&&(!1===r?(e=e+"?routeOrgName="+this.searchCondition.routeOrgName,r=!0):e=e+"&routeOrgName="+this.searchCondition.routeOrgName),""!==this.searchCondition.srcDcName&&(!1===r?(e=e+"?srcDcName="+this.searchCondition.srcDcName,r=!0):e=e+"&srcDcName="+this.searchCondition.srcDcName),""!==this.searchCondition.dstDcName&&(!1===r?(e=e+"?dstDcName="+this.searchCondition.dstDcName,r=!0):e=e+"&dstDcName="+this.searchCondition.dstDcName),""!==this.searchCondition.tag&&(!1===r?(e=e+"?tag="+this.searchCondition.tag,r=!0):e=e+"&tag="+this.searchCondition.tag),!1===r?(e=e+"?deleted="+this.searchCondition.deleted,r=!0):e=e+"&deleted="+this.searchCondition.deleted,console.log("uri: {}",e),t.axios.get(e).then((function(e){console.log(e),t.total=e.data.data.length,t.routes=e.data.data}))},handleChangeSize:function(t){this.size=t},goToLink:function(t,e){1===t.deleted?alert("已经下线，请先回滚！"):(console.log("go to manage route for"+t.routeOrgName+"-"+t.tag+", from "+t.srcDcName+" to "+t.dstDcName),this.$router.push({path:"/proxyRouteManagement",query:{routeOrgName:t.routeOrgName,srcDcName:t.srcDcName,dstDcName:t.dstDcName,tag:t.tag,srcProxyUris:t.srcProxyUris,relayProxyUris:t.relayProxyUris,dstProxyUris:t.dstProxyUris,updateStatus:!0}}))},goToDelete:function(t,e){var r=this;1===t.deleted?alert("已经下线，无需操作！"):this.axios.post("/api/drc/v1/meta/routes",{id:0,routeOrgName:t.routeOrgName,srcDcName:t.srcDcName,dstDcName:t.dstDcName,srcProxyUris:t.srcProxyUris,relayProxyUris:t.relayProxyUris,dstProxyUris:t.dstProxyUris,tag:t.tag,deleted:1}).then((function(t){console.log(t.data),console.log(t.data.data),0===t.data.status?(alert("下线成功！"),r.searchCondition.deleted=1,r.getRoutes()):alert("操作失败！")}))},goToRecover:function(t,e){var r=this;0===t.deleted?alert("正在使用中无需回滚！"):this.axios.post("/api/drc/v1/meta/routes",{id:0,routeOrgName:t.routeOrgName,srcDcName:t.srcDcName,dstDcName:t.dstDcName,srcProxyUris:t.srcProxyUris,relayProxyUris:t.relayProxyUris,dstProxyUris:t.dstProxyUris,tag:t.tag,deleted:0}).then((function(t){console.log(t.data),console.log(t.data.data),0===t.data.status?(alert("回滚成功！"),r.searchCondition.deleted=0,r.getRoutes()):alert("操作失败！")}))},goToRouteManagementLink:function(){console.log("go to manage route"),this.$router.push({path:"/proxyRouteManagement",query:{updateStatus:!1,srcProxyUris:[],relayProxyUris:[],dstProxyUris:[]}})}},created:function(){this.getRoutes()}},s=a,c=r("2877"),u=Object(c["a"])(s,n,o,!1,null,"55f9f636",null);e["default"]=u.exports},fb6a:function(t,e,r){"use strict";var n=r("23e7"),o=r("861d"),i=r("e8b5"),a=r("23cb"),s=r("50c4"),c=r("fc6a"),u=r("8418"),l=r("b622"),d=r("1dde"),f=r("ae40"),g=d("slice"),h=f("slice",{ACCESSORS:!0,0:0,1:2}),y=l("species"),p=[].slice,m=Math.max;n({target:"Array",proto:!0,forced:!g||!h},{slice:function(t,e){var r,n,l,d=c(this),f=s(d.length),g=a(t,f),h=a(void 0===e?f:e,f);if(i(d)&&(r=d.constructor,"function"!=typeof r||r!==Array&&!i(r.prototype)?o(r)&&(r=r[y],null===r&&(r=void 0)):r=void 0,r===Array||void 0===r))return p.call(d,g,h);for(n=new(void 0===r?Array:r)(m(h-g,0)),l=0;g<h;g++,l++)g in d&&u(n,l,d[g]);return n.length=l,n}})},fdbc:function(t,e){t.exports={CSSRuleList:0,CSSStyleDeclaration:0,CSSValueList:0,ClientRectList:0,DOMRectList:0,DOMStringList:0,DOMTokenList:1,DataTransferItemList:0,FileList:0,HTMLAllCollection:0,HTMLCollection:0,HTMLFormElement:0,HTMLSelectElement:0,MediaList:0,MimeTypeArray:0,NamedNodeMap:0,NodeList:1,PaintRequestList:0,Plugin:0,PluginArray:0,SVGLengthList:0,SVGNumberList:0,SVGPathSegList:0,SVGPointList:0,SVGStringList:0,SVGTransformList:0,SourceBufferList:0,StyleSheetList:0,TextTrackCueList:0,TextTrackList:0,TouchList:0}}}]);
//# sourceMappingURL=chunk-74447852.863c5d3a.js.map