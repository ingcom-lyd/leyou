package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecificationService {
    @Autowired
    private SpecificationMapper specMapper;

    @Transactional
    public void saveSpecification(Long categoryId, String specifications) {
        Specification specification = new Specification();
        specification.setCategoryId(categoryId);
        specification.setSpec(specifications);

        int result = specMapper.insert(specification);
        if (result != 1) {
            throw new LyException(ExceptionEnum.SPEC_SAVA_ERROR);
        }

    }

    @Transactional
    public void updateSpecification(Long categoryId, String specifications) {
        Specification specification = new Specification();
        specification.setCategoryId(categoryId);
        specification.setSpec(specifications);

        int result = specMapper.updateByPrimaryKeySelective(specification);
        if (result != 1) {
            throw new LyException(ExceptionEnum.SPEC_UPDATE_ERROR);
        }

    }

//    public List<Specification> queryGroupById(Long cid) {
//        Specification specGroup = new Specification();
//        specGroup.setCid(cid);
//
//        List<Specification> list = specGroupMapper.select(specGroup);
//
//        if(CollectionUtils.isEmpty(list)){
//            //没查到
//            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
//        }
//
//        return list;
//    }

    public String querySpecificationById(Long cid) {
        Specification specGroup = specMapper.selectByPrimaryKey(cid);

        if (specGroup == null) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        return specGroup.getSpec();
    }

    @Transactional
    public void deleteSpecificationById(Long categoryId) {
        int result = specMapper.deleteByPrimaryKey(categoryId);
        if (result != 1){
            throw new LyException(ExceptionEnum.SPEC_DELETE_ERROR);
        }
    }
}
