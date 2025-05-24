package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category
{
    private int id;
    private String name;
    private Integer parent_id;

    public Category(String name, Integer parent_id)
    {
        this.name = name;
        this.parent_id = parent_id;
    }
    
    public Integer getParentId() {
        return parent_id;
    }
    
    public void setParentId(Integer parentId) {
        this.parent_id = parentId;
    }
}
