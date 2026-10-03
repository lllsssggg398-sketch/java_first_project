package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Override
    public void customService(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate=apartmentSubmitVo.getId()!=null;
        saveOrUpdate(apartmentSubmitVo); //mybatis‑plus
        Long apartmentId= apartmentSubmitVo.getId();
        if(isUpdate){
        Long id=apartmentSubmitVo.getId();
        //公寓配套中间表
            LambdaQueryWrapper<ApartmentFacility> lambdaQueryWrapper=new LambdaQueryWrapper<>();
           lambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
           apartmentFacilityService.remove(lambdaQueryWrapper);
        //公寓杂费中间表
        LambdaQueryWrapper<ApartmentFeeValue> lambdaQueryWrapper1=new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(ApartmentFeeValue::getFeeValueId,id);
        apartmentFeeValueService.remove(lambdaQueryWrapper1);
        //公寓标签中间表
        LambdaQueryWrapper<ApartmentLabel> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(lambdaQueryWrapper2);
        //公寓图片
        LambdaQueryWrapper<GraphInfo> lambdaQueryWrapper3=new LambdaQueryWrapper<>();
        lambdaQueryWrapper3.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        lambdaQueryWrapper3.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(lambdaQueryWrapper3);
        }
        //3.保存数据[公寓数据，其他的数据]
        //保存公寓和配套中间表
        List<Long> facilityInfolds=apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfolds)){
            List<ApartmentFacility> facilities=new ArrayList<>(facilityInfolds.size());
            for(Long facilityInfold:facilityInfolds){
                ApartmentFacility apartmentFacility=ApartmentFacility.builder().apartmentId(apartmentId).facilityId(facilityInfold).build();
                facilities.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(facilities);
        }
        //保存公寓和标签中间表
        List<Long> labelsIds=apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(labelsIds)){
            List<ApartmentLabel> apartmentLabels=new ArrayList<>(labelsIds.size());
            for (Long labelsId : labelsIds) {
                ApartmentLabel apartmentLabel = ApartmentLabel.builder().apartmentId(apartmentId).labelId(labelsId).build();
                apartmentLabels.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabels);
        }
        //保存公寓和杂费value中间表
        List<Long> feeValues=apartmentSubmitVo.getFeeValueIds();
        //集合判断非空
        if(!CollectionUtils.isEmpty(feeValues)){
            List<ApartmentFeeValue> apartmentFeeValues=new ArrayList<>(feeValues.size());
            for (Long feeValue : feeValues) {
                ApartmentFeeValue apartmentFeeValue=ApartmentFeeValue.builder().apartmentId(apartmentId).feeValueId(feeValue).build();
                apartmentFeeValues.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValues);
        }
        //保存公寓图片表数据
        List<GraphVo> graphVoList=apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            List<GraphInfo> graphInfos=new ArrayList<>(graphVoList.size());
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(graphVo.getName());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setItemId(apartmentId);
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfos.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfos);
        }
    }

    @Override
    public void customRemoveByid(Long id) {
        //先查询公寓有没有房间 有提示207
        LambdaQueryWrapper<RoomInfo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RoomInfo::getApartmentId,id);
        long count = roomInfoService.count(lambdaQueryWrapper);
        if(count>0){
            throw new LeaseException(ResultCodeEnum.DELETE_ERROR);
        }
        removeById(id);
        //删除四个关系表数据库
        //delete photos
        LambdaQueryWrapper<GraphInfo> lambdaQueryWrapper1=new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(GraphInfo::getItemType,ItemType.APARTMENT);
        lambdaQueryWrapper1.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(lambdaQueryWrapper1);
        //删除配套
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper=new LambdaQueryWrapper<>();
        apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);
        //标签
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(apartmentLabelLambdaQueryWrapper);
        //杂费
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(apartmentFeeValueLambdaQueryWrapper);
    }

    @Override
    public IPage<ApartmentItemVo> pageApartmentItemByQuery(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pagrApartmentItemByQuery(page,queryVo);
    }

    @Override
    public ApartmentDetailVo apartmentinfoById(Long id) {
        ApartmentInfo apartmentInfo=this.getById(id);
        if(apartmentInfo==null){
            return null;
        }
        //2.查询GraphInfo
        List<GraphVo> graphVoList=graphInfoMapper.queryList(ItemType.APARTMENT,id);
        //3.查询LabelInfo
        List<LabelInfo> labelInfoList=labelInfoMapper.queryLabel(id);
        //4.查询FacilityInfo
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.queryList(id);
        //5.查询FeeValue
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);
        ApartmentDetailVo apartmentDetailVo=new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);

        return apartmentDetailVo;
    }
}




