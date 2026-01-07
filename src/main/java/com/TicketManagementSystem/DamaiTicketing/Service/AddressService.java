package com.TicketManagementSystem.DamaiTicketing.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.TicketManagementSystem.DamaiTicketing.Entity.Address;
import com.TicketManagementSystem.DamaiTicketing.Exception.BusinessException;
import com.TicketManagementSystem.DamaiTicketing.Mapper.AddressMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    // 获取收获地址
    public List<Address> getAddress() {

        // 比 Manager类更加优雅的简略写法
        List<Address> result = this.lambdaQuery()
                .eq(Address::getUserId, StpUtil.getLoginIdAsLong())
                .list();
        if (!result.isEmpty()) throw new BusinessException(404, "您暂未设置地址");
        return result;
    }

    // 修改收获地址
    @Transactional
    public void updateAddress(Address address) {

        boolean result = this.lambdaUpdate()
                .eq(Address::getUserId, StpUtil.getLoginIdAsLong())
                .eq(Address::getId, address.getId())
                .set(address.getRecipientName() != null, Address::getRecipientName, address.getRecipientName())
                .set(address.getRecipientPhone() != null, Address::getRecipientPhone, address.getRecipientPhone())
                .set(address.getProvince() != null, Address::getProvince, address.getProvince())
                .set(address.getCity() != null, Address::getCity, address.getCity())
                .set(address.getDistrict() != null, Address::getDistrict, address.getDistrict())
                .set(address.getDetailAddress() != null, Address::getDetailAddress, address.getDetailAddress())
                .set(address.getIsDefaultAddress() != null, Address::getIsDefaultAddress, address.getIsDefaultAddress())
                .set(Address::getUpdateTime, LocalDateTime.now())
                .update();

        if (!result) throw new BusinessException(400, "修改失败");

    }

    // 删除收获地址
    public void deleteAddress(Long id) {

        boolean result = this.lambdaUpdate()
                .eq(Address::getUserId, StpUtil.getLoginIdAsLong())
                .eq(Address::getId, id)
                .remove();

        if (!result) throw new BusinessException(404, "删除地址失败");

    }

}
