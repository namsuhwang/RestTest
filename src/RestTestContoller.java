
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ini4j.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestTestContoller { 
 
    public static void main(String[] args) throws IOException, InterruptedException {
    	String reqDiv = "DVT"; // DPI, DVT
    	String reqDate = "20240628";
    	String reqBizDiv = "FAX";
    	String targetServer = "r";
    	String rootDirStr = "";
    	// rootDirStr = "D:\\Work\\하나손보\\04. 테스트\\2024-04-29 팩스외 채널 하나손해보험-채널샘플파일\\COMMON\\660a00b4e18dc101f609e608.jpg";    	
    	rootDirStr = "D:\\Work\\하나손보\\04. 테스트\\테스트결과\\완료\\JCD1000602_가입설계동의서\\입력\\2024031801091100.ini";
    	
        HttpClient client = HttpClient.newHttpClient();
        String boundary = CommonUtil.generateBoundary();       
        
        if(reqDiv.equals("DPI")) {
        	String targetFileStr = rootDirStr;
        	File targetFile = new File(targetFileStr);
        	System.out.println("img file : " + targetFile.getName());
        	Map<Object, Object> formDataMap = new HashMap<Object, Object>();
        	
        	String url = "";
        	
        	formDataMap.put("file", Path.of(targetFile.toString()));
        	String targetServerIp = "";
        	switch(targetServer) {
	        	case "d": targetServerIp = "10.154.20.86"; break;
	        	case "s": targetServerIp = "10.154.120.86"; break;
	        	case "p": targetServerIp = "10.153.20.120"; break;
	        	case "r": targetServerIp = "10.0.9.33"; break;
        	}
        	url = "http://" + targetServerIp + ":5000/v1/packages/cvt.hana_insu/dpi";
        	
        	HttpRequest.BodyPublisher bp = CommonUtil.multipartToByte(formDataMap, boundary);
        	
        	System.out.println("Request : " + formDataMap.toString());
        	
        	HttpRequest request = HttpRequest.newBuilder()
        			.uri(URI.create(url))
        			.header("Content-Type", "multipart/form-data; boundary=" + boundary)
        			.POST(bp)
        			.build();
        	HttpResponse<String> response = client.send(request,  HttpResponse.BodyHandlers.ofString());
        	System.out.println("Response : " + response.body());
        }else {

    		List<TargetFileInfo> targetFileList = new ArrayList<>();
    		List<TargetFileInfo> infoFileList = new ArrayList<>();
    		
    		File rootObj = new File(rootDirStr);
    		if(rootObj.isFile()) {
    			String targetFileNameWithoutExt = CommonUtil.getFileNameWithoutExt(rootObj.getName());
    			rootDirStr = rootObj.getParent();
    			List<File> subFileList = CommonUtil.getFileListInDir(rootDirStr, null, null);
    			if(subFileList != null && subFileList.size() > 0) {
    				for(File f : subFileList) {
    					if(f.isDirectory()) {
    						continue;
    					}
    					
    					String fileNameWithoutExt = CommonUtil.getFileNameWithoutExt(f.getName());
    					if(targetFileNameWithoutExt.equals(fileNameWithoutExt)) {
    						TargetFileInfo tfi = new TargetFileInfo(f.getName(), f);
    						if(tfi.getFileExt().toUpperCase().equals("INI") || tfi.getFileExt().toUpperCase().equals("JSON")) {
    							infoFileList.add(new TargetFileInfo(f.getName(), f));
    						}else {
    							targetFileList.add(new TargetFileInfo(f.getName(), f));
    						}
    					}
    					
    					if(infoFileList != null && infoFileList.size() > 0 && targetFileList != null && targetFileList.size() > 0) {
    						break;
    					}
    				}
    			}
    		}else {
    			List<File> rootFileList = CommonUtil.getFileListInDir(rootDirStr, null, null);
    			if(rootFileList != null && rootFileList.size() > 0) {
    				for(File f : rootFileList) {
    					if(f.isDirectory()) {
    						continue;
    					}

						TargetFileInfo tfi = new TargetFileInfo(f.getName(), f);
						if(tfi.getFileExt().toUpperCase().equals("INI") || tfi.getFileExt().toUpperCase().equals("JSON")) {
							infoFileList.add(new TargetFileInfo(f.getName(), f));
						}else {
							targetFileList.add(new TargetFileInfo(f.getName(), f));
						}
    				}
    			}
    			
    			// 하위 디렉토리
    			List<String> subDirList = CommonUtil.getDirList(rootDirStr);
    			
    			for(String dirPath : subDirList) {
    				String pathStr = Path.of(rootDirStr, dirPath).toString();
    				List<File> subFileList = CommonUtil.getFileListInDir(pathStr, null, null);
    				if(subFileList != null && subFileList.size() > 0) {
    					for(File f : subFileList) {
        					if(f.isDirectory()) {
        						continue;
        					}

    						TargetFileInfo tfi = new TargetFileInfo(f.getName(), f);
    						if(tfi.getFileExt().toUpperCase().equals("INI") || tfi.getFileExt().toUpperCase().equals("JSON")) {
    							infoFileList.add(new TargetFileInfo(f.getName(), f));
    						}else {
    							targetFileList.add(new TargetFileInfo(f.getName(), f));
    						}
    					}
    				}
    			}
    		}

            
            // 전송
            for(TargetFileInfo infoFile : infoFileList) {
            	try {
            		Thread.sleep(10);            		
            	}catch(InterruptedException e) {
            		e.printStackTrace();
            	}
            	
            	File targetFile = null;
            	for(TargetFileInfo t : targetFileList) {
            		if(t.getFileName().equals(infoFile.getFileName())) {
            			targetFile = t.file;
            		}
            	}
            	
            	if(targetFile == null) {
            		System.out.println("Unmatch ext => skip : " + infoFile.getFileFullName());
            		continue;
            	}else {
            		System.out.println("info file : " + infoFile.getFileFullName());
            		System.out.println("img file : " + targetFile.getName());
            	}
            	
            	Map<Object, Object> formDataMap = new HashMap<Object, Object>();
            	String bizDiv = "";
            	if(reqBizDiv != null) {
            		bizDiv = reqBizDiv.toUpperCase();
            	}else {
            		bizDiv = CommonUtil.getBizDivFromPath(infoFile.getFile());
            	}
            	
            	String url = "";
            	
            	formDataMap.put("file", Path.of(targetFile.toString()));
            	formDataMap.put("biz-div", bizDiv);
            	formDataMap.put("request-date", reqDate);
            	if(bizDiv.equals("FAX")) {
                	formDataMap.put("request-data", "{}");
                	String targetServerIp = "";
                	switch(targetServer) {
        	        	case "d": targetServerIp = "10.154.20.86"; break;
        	        	case "s": targetServerIp = "10.154.120.86"; break;
        	        	case "p": targetServerIp = "10.153.20.130"; break;
        	        	case "r": targetServerIp = "10.0.9.33"; break;
                	}
                    url = "http://" + targetServerIp + ":5000/v1/packages/frc.hana_insu/recognize";                	
            	}else {
                	formDataMap.put("request-data", CommonUtil.getJsonFromInfoFile(infoFile));
                	String targetServerIp = "";
                	switch(targetServer) {
        	        	case "d": targetServerIp = "10.154.20.86"; break;
        	        	case "s": targetServerIp = "10.154.120.86"; break;
        	        	case "p": targetServerIp = "10.153.20.120"; break;
        	        	case "r": targetServerIp = "10.0.9.33"; break;
                	}
                    url = "http://" + targetServerIp + ":5000/v1/packages/cvt.hana_insu/convert";   
            	}
            	            	            	
            	HttpRequest.BodyPublisher bp = CommonUtil.multipartToByte(formDataMap, boundary);
            	
            	System.out.println("Request : " + formDataMap.toString());
            	
            	HttpRequest request = HttpRequest.newBuilder()
            			.uri(URI.create(url))
            			.header("Content-Type", "multipart/form-data; boundary=" + boundary)
            			.POST(bp)
            			.build();
            	HttpResponse<String> response = client.send(request,  HttpResponse.BodyHandlers.ofString());
            	System.out.println("Response : " + response.body());
            }
        }

        
        
    }


    
}
