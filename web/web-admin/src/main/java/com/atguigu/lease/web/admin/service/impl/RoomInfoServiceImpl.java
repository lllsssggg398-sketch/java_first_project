package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
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
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Override
    public void saveorupdateroominfo(RoomSubmitVo roomSubmitVo) {
        Boolean isupdate= roomSubmitVo.getId()!=null;
        saveOrUpdate(roomSubmitVo);
        if(isupdate){
            //delete RoomLeaseTerm
            LambdaQueryWrapper<RoomLeaseTerm> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId,roomSubmitVo.getId());
            roomLeaseTermService.removeById(lambdaQueryWrapper);
            //delete RoomPaymentType
            LambdaQueryWrapper<RoomPaymentType> lambdaQueryWrapper1=new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(RoomPaymentType::getRoomId,roomSubmitVo.getId());
            roomPaymentTypeService.removeById(lambdaQueryWrapper1);
            //delete RoomAttrValues
            LambdaQueryWrapper<RoomAttrValue> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
            lambdaQueryWrapper2.eq(RoomAttrValue::getRoomId,roomSubmitVo.getId());
            roomAttrValueService.removeById(lambdaQueryWrapper2);
            //delete RoomLabel
            LambdaQueryWrapper<RoomLabel> lambdaQueryWrapper3=new LambdaQueryWrapper<>();
            lambdaQueryWrapper3.eq(RoomLabel::getRoomId,roomSubmitVo.getId());
            roomLabelService.removeById(lambdaQueryWrapper3);
            //delete Facility
            LambdaQueryWrapper<RoomFacility> lambdaQueryWrapper4=new LambdaQueryWrapper<>();
            lambdaQueryWrapper4.eq(RoomFacility::getRoomId,roomSubmitVo.getId());
            roomFacilityService.removeById(lambdaQueryWrapper4);
            //delete graphInfo
            LambdaQueryWrapper<GraphInfo> lambdaQueryWrapper5=new LambdaQueryWrapper<>();
            lambdaQueryWrapper5.eq(GraphInfo::getItemId,roomSubmitVo.getId());
            lambdaQueryWrapper5.eq(GraphInfo::getItemType, ItemType.ROOM);
        }
        //保存graphInfo
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfos=new ArrayList<>(graphVoList.size());
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo=new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfos.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfos);
        }
        //保存RoomLeaseInfo
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIds)){
            ArrayList<RoomLeaseTerm> roomLeaseTerms=new ArrayList<>(leaseTermIds.size());
            for (Long paymentTypeId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(paymentTypeId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }
        //保存RoomPaymentTypeInfo
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypes=new ArrayList<>(paymentTypeIds.size());
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                roomPaymentTypes.add(roomPaymentType);
            }
           roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }
        //保存RoomAttrValueInfo
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValues=new ArrayList<>(attrValueIds.size());
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(attrValueId).build();
                roomAttrValues.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValues);
        }
        //保存RoomLabelInfo
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabels=new ArrayList<>(labelInfoIds.size());
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabels.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabels);
        }
        //保存RoomfacilityInfo
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilities=new ArrayList<>(facilityInfoIds.size());
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }
    }

    @Override
    public IPage<RoomItemVo> queryListByIPage(IPage<RoomItemVo> iPage, RoomQueryVo queryVo) {
        return roomInfoMapper.queryListByIpage(iPage,queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {
        //1.查询RoomInfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);

        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        //3.查询graphInfoList
        List<GraphVo> graphVoList = graphInfoMapper.queryList(ItemType.ROOM, id);

        //4.查询attrValueList
        List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

        //5.查询facilityInfoList
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);


        RoomDetailVo adminRoomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, adminRoomDetailVo);

        adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrvalueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }

    @Override
    public void removeroomById(Long id) {
        //1.删除RoomInfo
        removeById(id);

        //2.删除graphInfoList
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        //3.删除attrValueList
        LambdaQueryWrapper<RoomAttrValue> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryWrapper);

        //4.删除facilityInfoList
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityQueryWrapper);

        //5.删除labelInfoList
        LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelQueryWrapper);

        //6.删除paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentQueryWrapper);

        //7.删除leaseTermList
        LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
        termQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(termQueryWrapper);
    }
}




