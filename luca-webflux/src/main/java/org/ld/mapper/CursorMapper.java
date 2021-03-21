package org.ld.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.ld.schedule.ScheduleJob;

@Mapper
public interface CursorMapper {

    /**
     * 流式查询
     * https://www.cnblogs.com/goloving/p/9241421.html
     */
    @Select("select * from job limit #{limit}")
    Cursor<ScheduleJob> scan(@Param("limit") int limit);
}
