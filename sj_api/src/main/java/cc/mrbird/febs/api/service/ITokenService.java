package cc.mrbird.febs.api.service;

public interface ITokenService {
	String getAccessToken() throws Exception;
	String getJsToken() throws Exception;
}
