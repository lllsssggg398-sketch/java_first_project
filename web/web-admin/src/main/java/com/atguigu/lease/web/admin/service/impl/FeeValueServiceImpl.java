package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.web.admin.mapper.FeeKeyMapper;
import com.atguigu.lease.web.admin.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.admin.service.FeeValueService;
import com.atguigu.lease.web.admin.mapper.FeeValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【fee_value(杂项费用值表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue>
    implements FeeValueService{
    @Autowired
    FeeKeyMapper feeKeyMapper;
    @Override
    public List<FeeKeyVo> feelist() {
        return feeKeyMapper.feelist();
    }
}




