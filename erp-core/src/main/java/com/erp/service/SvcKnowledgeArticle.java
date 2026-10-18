package com.erp.service;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Knowledge base article (M-SVC-003). Linked to tickets and surfaced by full-text search from
 * the agent console and the customer portal.
 */
@Entity(name = "SvcKnowledgeArticle")
@Table(name = "svc_knowledge_article")
public class SvcKnowledgeArticle extends BaseEntity {

    /**
     * articleNumber
     */
    @Column(name = "article_number", length = 40, nullable = false)
    public String articleNumber;

    /**
     * title
     */
    @Column(name = "title", length = 200, nullable = false)
    public String title;

    /**
     * body
     */
    @Column(name = "body")
    public String body;

    /**
     * category
     */
    @Column(name = "category", length = 60)
    public String category;

    /**
     * itemId
     */
    @Column(name = "item_id")
    public Long itemId;

    /**
     * Article revision counter, incremented on each edit.
     */
    @Column(name = "revision")
    public Integer revision;

    /**
     * viewCount
     */
    @Column(name = "view_count")
    public Integer viewCount;

    /**
     * helpfulCount
     */
    @Column(name = "helpful_count")
    public Integer helpfulCount;

    /**
     * published
     */
    @Column(name = "published")
    public Boolean published;

    /**
     * author
     */
    @Column(name = "author", length = 120)
    public String author;

    @Override
    public String displayLabel() {
        return articleNumber + " - " + title;
    }

}
