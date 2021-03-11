package org.ld.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.pojo.JobExample;

import java.util.List;

@Mapper
public interface JobMapper {
    long countByExample(JobExample example);

    int deleteByExample(JobExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ScheduleJob record);

    int insertSelective(ScheduleJob record);

    List<ScheduleJob> selectByExample(JobExample example);

    ScheduleJob selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ScheduleJob record, @Param("example") JobExample example);

    int updateByExample(@Param("record") ScheduleJob record, @Param("example") JobExample example);

    int updateByPrimaryKeySelective(ScheduleJob record);

    int updateByPrimaryKey(ScheduleJob record);

}