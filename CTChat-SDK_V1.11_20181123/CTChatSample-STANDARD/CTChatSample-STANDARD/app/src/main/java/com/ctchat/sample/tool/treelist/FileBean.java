package com.ctchat.sample.tool.treelist;

import com.ctchat.sdk.ptt.tool.entity.ContactEntity;

public class FileBean {
    @com.ctchat.sample.tool.treelist.TreeNodeId
    private String _id;
    @TreeNodeParentId
    private String parentId;
    @TreeNodeLabel
    private String name;
    @com.ctchat.sample.tool.treelist.TreeNodeNumber
    private String no;
    @TreeNodeType
    private int genre;
    @TreeNodePId
    private String pId;
    @TreeNodeEntity
    private ContactEntity contactEntity;
    private long length;
    private String desc;

    public FileBean(String _id, String parentId, String name, String no, int genre, String pId,ContactEntity contactEntity) {
        this._id = _id;
        this.parentId = parentId;
        this.name = name;
        this.no = no;
        this.genre = genre;
        this.pId = pId;
        this.contactEntity = contactEntity;
    }

    public void setContactEntity(ContactEntity contactEntity) {
        this.contactEntity = contactEntity;
    }

    public ContactEntity getContactEntity() {
        return contactEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }


    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }
}
