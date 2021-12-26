package com.edaoren.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.edaoren.api.entity.User;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Repository;

/**
 * @author EDaoren
 */
@Repository
public interface UserMapper extends BaseMapper<User> {


    @Select("select * from t_user t ${ew.customSqlSegment}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(User.class)
    void getOrgWithBigData(@Param(Constants.WRAPPER) QueryWrapper<User> wrapper, ResultHandler<User> handler);

}
