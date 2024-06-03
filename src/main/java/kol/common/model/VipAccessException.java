package kol.common.model;

public class VipAccessException extends AppException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VipAccessException() {
		super("VIP_ACCESS_ERROR", "策略仅限会员跟单");
	}

}
