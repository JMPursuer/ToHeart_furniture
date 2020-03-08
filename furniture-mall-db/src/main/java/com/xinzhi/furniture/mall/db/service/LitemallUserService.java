package com.xinzhi.furniture.mall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.db.dao.LitemallUserMapper;
import com.xinzhi.furniture.mall.db.dao.LitemallWalletMapper;
import com.xinzhi.furniture.mall.db.domain.LitemallUser;
import com.xinzhi.furniture.mall.db.domain.LitemallUserExample;
import com.xinzhi.furniture.mall.db.domain.LitemallWallet;
import com.xinzhi.furniture.mall.db.domain.UserVo;
import com.xinzhi.furniture.mall.db.enumeration.UserStatusEnum;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.PageData;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.user.*;
import com.xinzhi.furniture.mall.db.service.resp.user.UserResp;
import com.xinzhi.furniture.mall.db.util.Constant;
import com.xinzhi.furniture.mall.db.util.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.xinzhi.furniture.mall.db.result.ErrorCodeEnum.PHONE_EXIST_ERROR;

@Service
@Transactional
public class LitemallUserService {
    @Resource
    private LitemallUserMapper userMapper;

    @Resource
    private LitemallWalletMapper walletMapper;

    public LitemallUser findById(Integer userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    public UserVo findUserVoById(Integer userId) {
        LitemallUser user = findById(userId);
        UserVo userVo = new UserVo();
        userVo.setNickname(user.getNickname());
        userVo.setAvatar(user.getAvatar());
        return userVo;
    }

    public LitemallUser queryByOid(String openId) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andWeixinOpenidEqualTo(openId).andDeletedEqualTo(false);
        return userMapper.selectOneByExample(example);
    }

    public void add(LitemallUser user) {
        user.setAddTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insertSelective(user);
    }

    public int updateById(LitemallUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateByPrimaryKeySelective(user);
    }

    public List<LitemallUser> querySelective(String username, String mobile, Integer page, Integer size, String sort, String order) {
        LitemallUserExample example = new LitemallUserExample();
        LitemallUserExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(username)) {
            criteria.andUsernameLike("%" + username + "%");
        }
        if (!StringUtils.isEmpty(mobile)) {
            criteria.andMobileEqualTo(mobile);
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return userMapper.selectByExample(example);
    }

    public int count() {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andDeletedEqualTo(false);

        return (int) userMapper.countByExample(example);
    }

    public List<LitemallUser> queryByUsername(String username) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public boolean checkByUsername(String username) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
        return userMapper.countByExample(example) != 0;
    }

    public List<LitemallUser> queryByMobile(String mobile) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andMobileEqualTo(mobile).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public List<LitemallUser> queryByOpenid(String openid) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andWeixinOpenidEqualTo(openid).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public void deleteById(Integer id) {
        userMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * 添加用户
     * @param userAddReq
     * @return
     */
    public Result addUser(UserAddReq userAddReq) {
        // 手机号码已存在
        LitemallUser litemallUser = userMapper.selectOneByExampleSelective(LitemallUserExample.newAndCreateCriteria().andMobileEqualTo(userAddReq.getMobile()).example());
        if(litemallUser != null) {
            return Result.fail(PHONE_EXIST_ERROR);
        }
        // 检查身份证号码是否已经存在
        LitemallUser ltUser = userMapper.selectOneByExampleSelective(LitemallUserExample.newAndCreateCriteria().andIdCardEqualTo(userAddReq.getIdCard()).example());
        if(ltUser != null) {
            return Result.fail(ErrorCodeEnum.ID_CARD_EXIST_ERROR);
        }

        LitemallUser user = new LitemallUser();
        BeanUtils.copyProperties(userAddReq, user);
        user.setAddTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setUsername(userAddReq.getMobile());
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
        user.setStatus(UserStatusEnum.NOT_UPDATE_PASSOWRD.getCode().byteValue());

        Float discount = userAddReq.getDiscount();
        if(discount != null) {
            if(discount > 0 && discount <= 1) {
                user.setDiscount(new BigDecimal(discount));
            }
        }

        userMapper.insertSelective(user);

        // 添加钱包
        LitemallWallet wallet = new LitemallWallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreateTime(Instant.now().toEpochMilli());
        wallet.setUserId(user.getId());
        walletMapper.insertSelective(wallet);

        return Result.ok();
    }

    /**
     * 修改用户
     * @param userUpdateReq
     * @retur
     */
    public Result updateUser(UserUpdateReq userUpdateReq) {
        LitemallUser user = userMapper.selectByPrimaryKey(userUpdateReq.getUserId());
        if(user == null) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST);
        }
        // 检查电话号码是否存在
        LitemallUser litemallUser = userMapper.selectOneByExampleSelective(LitemallUserExample.newAndCreateCriteria().andMobileEqualTo(userUpdateReq.getMobile()).example());
        if(litemallUser.getId() != user.getId()) {
            return Result.fail(ErrorCodeEnum.PHONE_EXIST_ERROR);
        }
        // 检查身份证号码是否已经存在
        LitemallUser ltUser = userMapper.selectOneByExampleSelective(LitemallUserExample.newAndCreateCriteria().andIdCardEqualTo(userUpdateReq.getIdCard()).example());
        if(ltUser.getId() != user.getId()) {
            return Result.fail(ErrorCodeEnum.ID_CARD_EXIST_ERROR);
        }
        BeanUtils.copyProperties(userUpdateReq, user);
        user.setUpdateTime(LocalDateTime.now());

        Float discount = userUpdateReq.getDiscount();
        if(discount != null) {
            if(discount > 0 && discount <= 1) {
                user.setDiscount(new BigDecimal(discount));
            }
        }

        userMapper.updateByPrimaryKeySelective(user);
        return Result.ok();
    }

    public Result updateUserStatus(UserStatusUpdateReq userStatusUpdateReq) {
        LitemallUser user = userMapper.selectByPrimaryKey(userStatusUpdateReq.getUserId());
        if(user == null) {
            return Result.fail(ErrorCodeEnum.USER_NOT_EXIST.getCode(), ErrorCodeEnum.USER_NOT_EXIST.getDesc());
        }
        LitemallUser litemallUser = new LitemallUser();
        litemallUser.setId(userStatusUpdateReq.getUserId());
        litemallUser.setStatus(userStatusUpdateReq.getStatus().byteValue());
        if(userStatusUpdateReq.getStatus().byteValue() == UserStatusEnum.NOT_UPDATE_PASSOWRD.getCode()) {
            litemallUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
        }
        userMapper.updateByPrimaryKeySelective(litemallUser);
        return Result.ok();
    }

    public Result<PageData<UserResp>> getUserList(UserListReq req) {
        LitemallUserExample example = new LitemallUserExample();
        LitemallUserExample.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(req.getMobile())) {
            criteria.andMobileEqualTo(req.getMobile());
        }
        criteria.andDeletedEqualTo(false);
        // 总数量
        long count = userMapper.countByExample(example);
        // 排序分页
        example.setOrderByClause(LitemallUser.Column.id.name() + " " + Constant.ORDER_DESC + " " + Constant.LIMIT + " " + (req.getPageIndex() - 1) * req.getPageSize() + "," + req.getPageSize());
        List<LitemallUser> litemallUserList = userMapper.selectByExample(example);

        List<UserResp> userRespList = Lists.newArrayList();
        for(LitemallUser user : litemallUserList) {
            UserResp userResp = new UserResp();
            BeanUtils.copyProperties(user, userResp);
            userResp.setUserId(user.getId());
            userRespList.add(userResp);
        }
        PageData pageData = new PageData();
        pageData.setPageIndex(req.getPageIndex());
        pageData.setPageSize(req.getPageSize());
        pageData.setTotal(count);
        pageData.setList(userRespList);
        return Result.ok().setResult(pageData);
    }

    public Result updatePassword(Integer userId, UserUpdatePasswordReq req) {
        if(!req.getPassword().equals(req.getRepassword())) {
            return Result.fail(ErrorCodeEnum.USER_PASSWORD_NOT_EQUAL_ERROR);
        }
        LitemallUser user = userMapper.selectByPrimaryKey(userId);
        if(user.getStatus() != UserStatusEnum.NOT_UPDATE_PASSOWRD.getCode().byteValue()) {
            return Result.fail(ErrorCodeEnum.USER_ALREADY_RESET_PASSWORD_ERROR);
        }
        LitemallUser litemallUser = new LitemallUser();
        litemallUser.setId(userId);
        litemallUser.setStatus(UserStatusEnum.OK.getCode().byteValue());
        litemallUser.setPassword(new BCryptPasswordEncoder().encode(req.getPassword()));
        userMapper.updateByPrimaryKeySelective(litemallUser);
        return Result.ok();
    }

    public Result wechatAuth(Integer userId, String openid) {
        LitemallUser user = new LitemallUser();
        user.setId(userId);
        user.setWeixinOpenid(openid);
        userMapper.updateByPrimaryKeySelective(user);
        return Result.ok();
    }

}
