package org.ld.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ld.pojo.ConfigProperties;
import org.ld.pojo.example.ConfigPropertiesExample;

@Mapper
public interface ConfigPropertiesMapper {
    long countByExample(ConfigPropertiesExample example);

    int deleteByExample(ConfigPropertiesExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ConfigProperties record);

    int insertSelective(ConfigProperties record);

    List<ConfigProperties> selectByExample(ConfigPropertiesExample example);

    ConfigProperties selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConfigProperties record, @Param("example") ConfigPropertiesExample example);

    int updateByExample(@Param("record") ConfigProperties record, @Param("example") ConfigPropertiesExample example);

    int updateByPrimaryKeySelective(ConfigProperties record);

    int updateByPrimaryKey(ConfigProperties record);

    void batchInsert(@Param("items") List<ConfigProperties> items);

    Long sumByExample(ConfigPropertiesExample example);

    ConfigProperties selectOneByExample(ConfigPropertiesExample example);

    int upsert(ConfigProperties record);

    int upsertSelective(ConfigProperties record);
}