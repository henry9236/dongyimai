package com.user.controller;
import java.util.List;

import com.dongyimai.bean.TbUser;
import com.dongyimai.result.PageResult;
import com.dongyimai.result.Result;
import com.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;

import java.util.regex.Pattern;
/**
 * 用户表controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 正则表达式：验证手机号
	 */
     public static final String REGEX_MOBILE ="^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String smsCode){
		if(!checkSmsCode(user.getPhone(),smsCode)){
			return new Result(false, "验证码输入错误！");
		}
		try {
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	//从redis中获取验证码,判断释放正确，以phone作为key 获取，验证码value
	public boolean checkSmsCode (String phone,String code){
		//得到缓存中的储存的验证码
		String sysCode = (String )redisTemplate.boundHashOps("smscode").get(phone);
		if(null==sysCode){
			return false;
		}
	 	return	sysCode.equals(code)? true: false;
	}
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);		
	}

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		//判断手机号格式
		if(!isMobile(phone)){
			return new Result(false, "手机号格式不正确");
		}
		try {
			userService.createSmsCode(phone);
			return new Result(true, "验证码发送成功");
		}catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "验证码发送失败");
		}

	}
	//对手机号进行正则校验
	public static boolean isMobile(String mobile) {
		return Pattern.matches(REGEX_MOBILE, mobile);
	}

}
