package org.ld.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ld.pojo.Adapter;
import org.ld.pojo.AdapterExample;

@Mapper
public interface AdapterMapper {
    long countByExample(AdapterExample example);

    int deleteByExample(AdapterExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Adapter record);

    int insertSelective(Adapter record);

    List<Adapter> selectByExample(AdapterExample example);

    Adapter selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Adapter record, @Param("example") AdapterExample example);

    int updateByExample(@Param("record") Adapter record, @Param("example") AdapterExample example);

    int updateByPrimaryKeySelective(Adapter record);

    int updateByPrimaryKey(Adapter record);
}