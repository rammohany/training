/**
 * @class CQ.wcm.AppsConfigManager
 * @extends CQ.Ext.Viewport The AppsConfigManager lets the user manage
 *          appsConfigNodes
 * @constructor Creates a new AppsConfigManager.
 * @param {Object}
 *            config The config object
 */

var canAddAppsconfig = false;
var canModifyAppsconfig = false;
var canDeleteAppsconfig = false;

function getPermissions($) {
    var appsServiceReq = null;
        appsServiceReq = jQuery.ajax( {
            url : "/apps/services/AppsConfigServlet.html?action=getPermissions",
            type : "POST",
            async : false,
            dataType : "json"
        });
    var data = eval('(' + appsServiceReq.responseText + ')');

    if (data.create) {
        canAddAppsconfig = true;
    }
    if (data.remove) {
        canDeleteAppsconfig = true;
    }
    if (data.modify) {
        canModifyAppsconfig = true;
    }
}
CQ.wcm.AppsConfigManager = function(config) {
    this.constructor.call(this, config);

};

var editFlag = false;

CQ.wcm.AppsConfigManager = CQ.Ext
        .extend(
                CQ.Ext.Viewport,
                {

                    viewConfig : {
                        forceFit : true
                    },

                    constructor : function(config) {
                        var myThis = this;
                        // store
                        var store = new CQ.Ext.data.Store(
                                {
                                    proxy : new CQ.Ext.data.HttpProxy(
                                            {
                                                url : "/apps/services/AppsConfigServlet.html?action=getall"
                                            }),
                                    autoLoad : true,
                                    reader : new CQ.Ext.data.JsonReader( {
                                        root : 'result',
                                        totalProperty : 'results',
                                        id : 'path',
                                        fields : [ 'path', 'propertyName',
                                                'authorValue', 'publishValue' ]
                                    })
                                });
                        store.setDefaultSort('path', 'ASC');

                        // column model
                        var cm = new CQ.Ext.grid.ColumnModel(
                                [
                                        new CQ.Ext.grid.RowNumberer(),
                                        {
                                            header : CQ.I18n
                                                    .getMessage("Property Name"),
                                            dataIndex : 'propertyName',
                                            width : 330,
                                            sortable : true
                                        },

                                        {
                                            header : CQ.I18n
                                                    .getMessage("Publish Value"),
                                            dataIndex : 'publishValue',
                                            width : 330,
                                            sortable : true
                                        },
                                        {
                                            header : CQ.I18n
                                                    .getMessage("Author Value"),
                                            dataIndex : 'authorValue',
                                            width : 330,
                                            sortable : true
                                        } ]);
                        // cm.defaultSortable = true;

                        var removeAction = new CQ.Ext.Action(
                                {
                                    cls : 'CQ.wcm.AppsConfigManager.remove',
                                    text : CQ.I18n.getMessage('Remove'),
                                    handler : function() {
                                        CQ.Ext.Msg
                                                .show( {
                                                    title : CQ.I18n
                                                            .getMessage('Delete Property ?'),
                                                    msg : CQ.I18n
                                                            .getMessage('Would you really like to delete the property configuration?'),
                                                    buttons : CQ.Ext.Msg.YESNO,
                                                    icon : CQ.Ext.MessageBox.QUESTION,
                                                    fn : function(btnId) {
                                                        if (btnId == 'yes') {
                                                            var selections = CQ.Ext
                                                                    .getCmp(
                                                                            'cq-appsconfig-grid')
                                                                    .getSelectionModel()
                                                                    .getSelections();
                                                            //console.log(selections);
                                                            //console.log(selections.length);
                                                            if (selections) {
                                                                var params = {};
                                                                params[CQ.Sling.STATUS] = CQ.Sling.STATUS_BROWSER;
                                                                params[CQ.Sling.OPERATION] = CQ.Sling.OPERATION_DELETE;
                                                                params[CQ.Sling.CHARSET] = "utf-8";
                                                                var length = selections.length;
                                                                for ( var i = 0; i < length; i++) {
                                                                    CQ.HTTP
                                                                            .post(
                                                                                    selections[i].id,
                                                                                    null,
                                                                                    params,
                                                                                    this);
                                                                }
                                                            }
                                                            CQ.Ext
                                                                    .getCmp('cq-appsconfig-grid').store
                                                                    .reload();
                                                        }
                                                    }
                                                });
                                    },
                                    tooltip : {
                                        title : CQ.I18n
                                                .getMessage('Remove configuration entry'),
                                        text : CQ.I18n
                                                .getMessage('Removes the selected Properties configuration'),
                                        autoHide : true
                                    }
                                });
                        removeAction.setDisabled(true);

                        var editAction = new CQ.Ext.Action(
                                {
                                    cls : 'CQ.wcm.AppsConfigManager.edit',
                                    text : CQ.I18n.getMessage('Edit'),
                                    handler : function() {
                                        editFlag = true;
                                        var selections = CQ.Ext.getCmp(
                                                'cq-appsconfig-grid')
                                                .getSelectionModel()
                                                .getSelections();
                                        if (selections) {
                                            if (selections.length > 1) {
                                                CQ.Ext.Msg
                                                        .show( {
                                                            title : CQ.I18n
                                                                    .getMessage('Edit appsconfig Configuration'),
                                                            msg : CQ.I18n
                                                                    .getMessage('Please select only one row'),
                                                            buttons : CQ.Ext.Msg.OK
                                                        });
                                                return;
                                            }
                                            //console.log(selections[0]);
                                            //console.log(selections[0].json.path);
                                            var path = selections[0].json.path;
                                            field = myThis.newDialog
                                                    .getField("nodeName");
                                            field.setValue(path);
                                            var propertyName = selections[0].json.propertyName;

                                            var authorValue = selections[0].json.authorValue;
                                            var publishValue = selections[0].json.publishValue;

                                            var field = myThis.newDialog
                                                    .getField("./propertyName");

                                            field.setValue(propertyName);
                                            field.setDisabled(true);

                                            field = myThis.newDialog
                                                    .getField("./authorValue");

                                            field.setValue(authorValue);

                                            field = myThis.newDialog
                                                    .getField("./publishValue");
                                            field.setValue(publishValue);

                                            field = myThis.newDialog
                                                    .getField("./encrypt");
                                            field.setValue('false');

                                            myThis.newDialog.show();

                                        }

                                    },
                                    tooltip : {
                                        title : CQ.I18n
                                                .getMessage('Edit configuration entry'),
                                        text : CQ.I18n
                                                .getMessage('Edit the selected property'),
                                        autoHide : true
                                    }
                                });
                        editAction.setDisabled(true);
                        // start publish
                        var publishAction = new CQ.Ext.Action(
                                {
                                    cls : 'CQ.wcm.AppsConfigManager.publish',
                                    text : CQ.I18n.getMessage('Publish'),
                                    handler : function() {
                                        CQ.Ext.Msg
                                                .show( {
                                                    title : CQ.I18n
                                                            .getMessage('Are you want sure publish?'),
                                                    msg : CQ.I18n
                                                            .getMessage('Would you really publish appsconfig nodes?'),
                                                    buttons : CQ.Ext.Msg.YESNO,
                                                    icon : CQ.Ext.MessageBox.QUESTION,
                                                    fn : function(btnId) {
                                                        if (btnId == 'yes') {
                                                            var selections = CQ.Ext
                                                                    .getCmp(
                                                                            'cq-appsconfig-grid')
                                                                    .getSelectionModel()
                                                                    .getSelections();
                                                            //console.log(selections);
                                                            //console.log(selections.length);
                                                            if (selections) {
                                                                var length = selections.length;
                                                                var publishidlist = new Array();
                                                                for ( var i = 0; i < length; i++) {

                                                                    publishidlist
                                                                            .push(selections[i].id);
                                                                }
                                                                var publishReq = null;
                                                                            publishReq = jQuery
                                                                                    .ajax( {
                                                                                        url : "/apps/services/AppsConfigServlet.html",
                                                                                        type : "POST",
                                                                                        async : false,
                                                                                        data : "action=publishNodes&publishid="
                                                                                                + publishidlist,
                                                                                        dataType : "json"

                                                                                    });
                                                                publishReq
                                                                        .done(appsShowStatus);

                                                            }
                                                        }
                                                    }
                                                });
                                    },
                                    tooltip : {
                                        title : CQ.I18n
                                                .getMessage('publish configuration entry'),
                                        text : CQ.I18n
                                                .getMessage('publish the selected apps config nodes '),
                                        autoHide : true
                                    }
                                });
                        publishAction.setDisabled(true);
                        // end publish
                        //start publish all
                        var publishAllAction = new CQ.Ext.Action(
                                {
                                    cls : 'CQ.wcm.AppsConfigManager.publishall',
                                    text : CQ.I18n.getMessage('Publish All'),
                                    handler : function() {
                                        CQ.Ext.Msg
                                                .show( {
                                                    title : CQ.I18n
                                                            .getMessage('Publish All?'),
                                                    msg : CQ.I18n
                                                            .getMessage('Do you want to publish all appsconfig nodes?'),
                                                    buttons : CQ.Ext.Msg.YESNO,
                                                    icon : CQ.Ext.MessageBox.QUESTION,
                                                    fn : function(btnId) {
                                                        if (btnId == 'yes') {
                                                                var publishAllReq = null;
                                                                            publishAllReq = jQuery
                                                                                    .ajax( {
                                                                                        url : "/apps/services/AppsConfigServlet.html",
                                                                                        type : "POST",
                                                                                        async : false,
                                                                                        data : "action=publishAllNodes",
                                                                                        dataType : "json"

                                                                                    });
                                                                publishAllReq
                                                                        .done(appsShowStatus);

                                                            }
                                                        }
                                                });
                                    },
                                    tooltip : {
                                        title : CQ.I18n
                                                .getMessage('Publish All'),
                                        text : CQ.I18n
                                                .getMessage('All appsconfig nodes successfully published'),
                                        autoHide : true
                                    }
                                });
                        //End publish all
                        var selectAllAction = new CQ.Ext.Action(
                                {
                                    cls : 'CQ.wcm.AppsConfigManager.selectall',
                                    text : CQ.I18n.getMessage('Select All'),
                                    handler : function() {
                                        CQ.Ext.getCmp('cq-appsconfig-grid')
                                                .getSelectionModel()
                                                .selectAll();
                                    },
                                    tooltip : {
                                        title : CQ.I18n
                                                .getMessage('Select All'),
                                        text : CQ.I18n
                                                .getMessage('Select all entries in the grid'),
                                        autoHide : true
                                    }
                                });

                        var deselectAllAction = new CQ.Ext.Action( {
                            cls : 'CQ.wcm.AppsConfigManager.deselectall',
                            text : CQ.I18n.getMessage('Clear Selection'),
                            handler : function() {
                                var selModel = CQ.Ext.getCmp(
                                        'cq-appsconfig-grid')
                                        .getSelectionModel();
                                selModel.clearSelections();
                            },
                            tooltip : {
                                title : CQ.I18n.getMessage('Clear Selection'),
                                text : CQ.I18n
                                        .getMessage('Clears all selections'),
                                autoHide : true
                            }
                        });

                        this.newDialog = CQ.WCM
                                .getDialog(
                                        {
                                            "jcr:primaryType" : "cq:Dialog",
                                            "xtype" : "dialog",
                                            "title" : 'Apps Config Configuration',
                                            "params" : {
                                                "_charset_" : "utf-8"
                                            },
                                            "items" : {
                                                "xtype" : 'panel',
                                                "items" : [
                                                        {
                                                            xtype : 'hidden',
                                                            name : 'action',
                                                            value : 'save'
                                                        },
                                                        {
                                                            xtype : 'hidden',
                                                            name : 'nodeName',
                                                            value : ''
                                                        },

                                                        {
                                                            "xtype" : "textfield",
                                                            "name" : "./propertyName",
                                                            "fieldLabel" : CQ.I18n
                                                                    .getMessage("Property Name:"),
                                                            "allowBlank" : false,
                                                            "vtype" : "appsConfigDuplicateCheck",
                                                            "vtypeText" : "Duplicate appsConfig Property",
                                                            "value" : "",
                                                            "validationDelay" : 1000,
                                                            "validator": function(){var publishValue=this.findParentByType('dialog').find("name",
                                                                    this.name)[0].getValue();
                                                            if(publishValue.replace(/^\s+/,'').replace(/\s+jQuery/,'')==''){return false;}
                                                            
                                                            else{return true;}
                                                            }
                                                        },
                                                        {
                                                            "xtype" : 'textfield',
                                                            "name" : './publishValue',
                                                            "fieldLabel" : CQ.I18n
                                                                    .getMessage("Publish Value:"),
                                                            "allowBlank" : false,
                                                            "required" : "true",
                                                            "value" : "",
                                                            "validator": function(){var publishValue=this.findParentByType('dialog').find("name",
                                                                    this.name)[0].getValue();
                                                            if(publishValue.replace(/^\s+/,'').replace(/\s+jQuery/,'')==''){return false;}
                                                            
                                                            else{return true;}
                                                            }
                                                        },
                                                        {
                                                            "xtype" : 'textfield',
                                                            "name" : './authorValue',
                                                            "fieldLabel" : CQ.I18n
                                                                    .getMessage("Author Value:"),
                                                            "value" : ""
                                                        },
                                                        {
                                                            "xtype" : 'checkbox',
                                                            "name" : './encrypt',
                                                            "fieldLabel" : CQ.I18n
                                                                    .getMessage("Encrypt"),
                                                            "checked" : false

                                                        } ]
                                            },
                                            "responseScope" : this,
                                            "success" : appsResponseHandler,
                                            "failure" : appsResponseHandler,
                                            "buttons" : CQ.Dialog.OKCANCEL
                                        }, 'appsConfig-dialog');

                        this.newDialog.form.url = '/apps/services/AppsConfigServlet.html';

                        var filterTextBox = new CQ.Ext.form.TextField( {
                            xtype : 'textfield',
                            fieldLabel : 'Type Filter String Here',
                            name : 'filter',
                            id : 'filter',
                            inputType : 'text',
                            enableKeyEvents : true,
                            value : ""
                        });
                        filterTextBox.on('keyup', function() {
                            var filterString = filterTextBox.getValue();
                            if (filterFields.getValue() === 'Select Filter') {
                                alert("Please Select Filter Field");

                            } else {
                                store.filter(filterFields.getValue(),
                                        filterString);
                            }
                        });

                        var filterFieldsStore = new CQ.Ext.data.JsonStore( {
                            root : 'filterFields',
                            fields : [ {
                                name : 'name',
                                type : 'string'
                            }, {
                                name : 'value',
                                type : 'string'
                            } ],
                            data : {
                                filterFields : [ {
                                    "name" : "Property Name",
                                    "value" : "propertyName"
                                }, {
                                    "name" : "Publish Value",
                                    "value" : "publishValue"
                                }, {
                                    "name" : "Author Value",
                                    "value" : "authorValue"
                                } ]
                            }
                        });

                        var filterFields = new CQ.Ext.form.ComboBox( {
                            id : 'filterFieldsSelect',
                            name : 'filterFieldsSelect',
                            store : filterFieldsStore,
                            displayField : 'name',
                            valueField : 'value',
                            value : 'Select Filter',
                            typeAhead : true,
                            editable : false,
                            mode : 'local',
                            triggerAction : 'all'
                        });

                        var tb = new CQ.Ext.Toolbar(
                                {
                                    "id" : "cq-feedimporter-toolbar",
                                    "items" : [
                                            {
                                                "xtype" : "button",
                                                "text" : CQ.I18n
                                                        .getMessage("Refresh"),
                                                "tooltip" : CQ.I18n
                                                        .getMessage("Updates the list of appsConfigNodes configurations"),
                                                "handler" : function() {
                                                    CQ.Ext
                                                            .getCmp('cq-appsconfig-grid').store
                                                            .reload();
                                                }
                                            },
                                            {
                                                "xtype" : "tbseparator"
                                            },
                                            {
                                                "xtype" : "button",
                                                "id" : "addButtonAppsconfig",
                                                "text" : CQ.I18n
                                                        .getMessage("Add"),
                                                "handler" : function() {
                                                    var field = myThis.newDialog
                                                            .getField("./propertyName");
                                                    field.setValue("");
                                                    field.setDisabled(false);
                                                    field = myThis.newDialog
                                                            .getField("./authorValue");
                                                    field.setValue("");
                                                    field = myThis.newDialog
                                                            .getField("./publishValue");
                                                    field.setValue("");
                                                    field = myThis.newDialog
                                                            .getField("nodeName");
                                                    field.setValue('');
                                                    field = myThis.newDialog
                                                            .getField("./encrypt");
                                                    field.setValue('false');
                                                    editFlag = false;
                                                    var selModel = CQ.Ext
                                                            .getCmp(
                                                                    'cq-appsconfig-grid')
                                                            .getSelectionModel();
                                                    selModel.clearSelections();
                                                    myThis.newDialog.show();
                                                }
                                            },
                                            removeAction,
                                            editAction,
                                            publishAction,
                                            publishAllAction,
                                            selectAllAction,
                                            deselectAllAction,
                                            {
                                                "xtype" : "tbseparator"
                                            },
                                            {
                                                xtype : 'label',
                                                text : 'Filter Field',
                                                style : 'padding-left:5px;padding-right:5px'
                                            },
                                            filterFields,
                                            {
                                                "xtype" : "tbseparator"
                                            },
                                            {
                                                xtype : 'label',
                                                style : 'padding-left:5px;padding-right:5px',
                                                text : 'Type Filter String Here'
                                            },

                                            filterTextBox

                                    ]
                                });

                        CQ.wcm.AppsConfigManager.superclass.constructor
                                .call(
                                        this,
                                        {
                                            "id" : "cq-appsconfig-wrapper",
                                            "renderTo" : "CQ",
                                            "layout" : "form",
                                            "items" : [ {
                                                xtype : "panel",
                                                layout : "fit",
                                                width : "100%",
                                                height : "595",
                                                tbar : tb,
                                                items : [ {
                                                    "xtype" : "grid",
                                                    "id" : "cq-appsconfig-grid",
                                                    "region" : "center",
                                                    "margins" : "5 5 5 5",
                                                    "loadMask" : true,
                                                    "stripeRows" : true,
                                                    "cm" : cm,
                                                    "store" : store,
                                                    "sm" : new CQ.Ext.grid.RowSelectionModel(
                                                            {
                                                                multiSelect : true,
                                                                listeners : {
                                                                    selectionchange : function(
                                                                            selectionModel) {
                                                                        removeAction
                                                                                .setDisabled(!(selectionModel
                                                                                        .hasSelection() && canDeleteAppsconfig));
                                                                        editAction
                                                                                .setDisabled(!(selectionModel
                                                                                        .hasSelection() && canModifyAppsconfig));
                                                                        publishAction
                                                                                .setDisabled(!selectionModel
                                                                                        .hasSelection());
                                                                        publishAllAction
                                                                        .setDisabled(!selectionModel
                                                                                .hasSelection());
                                                                    }
                                                                }
                                                            })
                                                } ]
                                            } ]
                                        });
                    },

                    syncSource : function() {
                        var dialog = this.newDialog;
                    },

                    initComponent : function() {
                        CQ.wcm.AppsConfigManager.superclass.initComponent
                                .call(this);
                    }
                });

var appsResponseHandler = function(form, opts) {
    var info = CQ.HTTP.eval(opts.response);
    if (info.success == true) {
        CQ.Ext.getCmp('cq-appsconfig-grid').store.reload();
    } else {
        CQ.Ext.Msg.alert(CQ.I18n.getMessage("Error"), CQ.I18n
                .getMessage("Could not create appsConfigNode configuration!"));
    }
};

CQ.Ext.reg("appsconfigmanager", CQ.wcm.AppsConfigManager);

CQ.Ext.form.VTypes["appsConfigDuplicateCheck"] = function(value, f) {
    if (!editFlag) {
        var nodePath = "";
        if (value) {
            var path = "/apps/services/AppsConfigServlet.html";
            var responseObj = null;
                responseObj = jQuery.ajax( {
                    url : path,
                    type : "POST",
                    dataType : "json",
                    async : false,
                    data : {
                        "action" : "validate",
                        "property" : value,
                        "nodeName" : nodePath
                    }

                }).responseText;

            //console.log(responseObj);
            return (responseObj == "true");
        }

        return false;
    }
    return true;

}
function appsShowStatus(data) {
    var publishSatus = data.status
    if (publishSatus == "true") {
        CQ.Ext.Msg
                .alert("Publish", "AppsConfigNodes published successfully...");
    } else if (publishSatus == "false") {
        CQ.Ext.Msg.alert("Publish", "AppsConfigNodes not published");
    } else {
        CQ.Ext.Msg.alert("Publish", data.status);
    }
}