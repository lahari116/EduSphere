package com.edusphere.identity.service;
 
import com.edusphere.identity.dto.UpdateUserRequest;
import com.edusphere.identity.entity.User;
 
import java.util.List;
 
public interface UserService {
 
    List<User> getAllUsers();
 
    User updateUser(Long id, UpdateUserRequest request);
 
    void softDeleteUser(Long id);
}