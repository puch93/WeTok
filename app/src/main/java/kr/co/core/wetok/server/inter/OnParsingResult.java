package kr.co.core.wetok.server.inter;


import kr.co.core.wetok.server.netUtil.HttpResult;

/*
서버에서 받아온 String 파싱하는 인터페이스
*/
public interface OnParsingResult {
    HttpResult onParse(String jsonString);
}

