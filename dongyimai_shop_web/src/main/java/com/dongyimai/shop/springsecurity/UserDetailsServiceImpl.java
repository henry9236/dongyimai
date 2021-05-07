package com.dongyimai.shop.springsecurity;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.bean.TbSeller;
import com.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    //创建sellerService，用于使用dao在数据库中查找数据
    @Reference
    private SellerService sellerService;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //在数据库中获取商家数据
        TbSeller seller = sellerService.findOne(s);
        if(null == seller){
            return null;
        }
        if("1".equals(seller.getStatus())){
            return new User(s,seller.getPassword(),grantedAuthorities);
        }else{
            return  null;
        }

    }
}
