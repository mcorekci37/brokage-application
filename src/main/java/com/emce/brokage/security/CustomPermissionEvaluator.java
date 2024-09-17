package com.emce.brokage.security;

import com.emce.brokage.auth.UserService;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.order.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserService userService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Custom logic to check permissions
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (targetDomainObject instanceof Order) {
            Order order = (Order) targetDomainObject;
            return customer.getId().equals(order.getCustomer().getId());
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
