import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtil {

    private static final String DOUBLE_HYPHEN = "--";
    private static final String LINEFEED = "\r\n";
    private static final String QUTATE = "\"";
    
	public static List<String> getDirList(String dirStr){
		File rootFile = new File(dirStr);
		String[] directories = rootFile.list((dir, name) -> new File(dir, name).isDirectory());
		List<String> subDirList = Arrays.asList(directories);	
		// subDirList.forEach(System.out::println);
		return subDirList;
	}
	
	public static List<File> getFileListInDir(String pathStr, String matchExt, String unmatchExt){
        List<File> fileList = new ArrayList<>();
        
        Path path = Path.of(pathStr);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
            stream.forEach(p -> {
                if (!Files.isDirectory(p)) { 
                	
                	// 확장자가 일치하는 건 추출
                	if(matchExt != null && matchExt.length() > 0) {
                    	if(getFileExt(p.toFile().getName()).toUpperCase().equals(matchExt.trim())) {
                            fileList.add(p.toFile());	
                    	}	
                	}

                	// 확장자가 동일하지 않은 건 추출
                	if(unmatchExt != null && unmatchExt.length() > 0) {
                    	if(!getFileExt(p.toFile().getName()).toUpperCase().equals(unmatchExt.trim())) {
                            fileList.add(p.toFile());	
                    	}	
                	}
                	
                	if(matchExt == null && unmatchExt == null) {
                        fileList.add(p.toFile());	
                	}
                }
            });
        } catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }

        // fileList.forEach(System.out::println);
        
        return fileList;
    }


	public static String getFileExt(String fileNameFullPath){
    	int idx = fileNameFullPath.lastIndexOf(".");
    	String ext = fileNameFullPath.substring(idx + 1);
    	return ext.trim();
	}

	public static String getFileNameWithoutExt(String fileNameFullPath){
    	int idx = fileNameFullPath.lastIndexOf(".");
    	String netFileName = fileNameFullPath.substring(0, idx);
    	return netFileName.trim();
	}
	
	// 파일명만 일치하는 다른 파일 1개 추출
	public static File getFileNameMatch(List<String> fileList, File file){
		file.getAbsoluteFile();
		String fileExt = getFileExt(file.getName());
		String fileName = getFileNameWithoutExt(file.getName());
		String fileFullName = fileName + "." + fileExt;
		int idx = fileFullName.lastIndexOf(".");
		
		for(String fileStr : fileList) {
			String fname = fileStr.substring(0, idx);
			String fext = getFileNameWithoutExt(fileStr);
			if(fname.equals(fileName) && !fileExt.equals(fext)) {
				File mfile = new File(fileStr);
				return mfile;
			}
		}  
        
        return null;
    }

	
	public static String generateBoundary() {
        return "----" + new BigInteger(128, new SecureRandom()).toString(16);
    }
	
	public static String getBizDivFromPath(File file) {
		String p = file.getAbsolutePath();
		String[] ary = p.split("\\\\");
		String bizDiv = "";
			
		for(int i = ary.length - 1; i >= 0; i--) {
			switch(ary[i].trim().toUpperCase()) {
				case "FAX": bizDiv = "FAX"; break;
				case "AOS": bizDiv = "AOS"; break;
				case "COM": bizDiv = "COM"; break;
				case "COMMON": bizDiv = "COMMON"; break;
				case "NOR": bizDiv = "NOR"; break;
				case "NORMAL": bizDiv = "NORMAL"; break;
				case "MO": bizDiv = "MO"; break;
				case "SFA": bizDiv = "SFA"; break;
			}
			
			if(!bizDiv.isBlank()) {
				return bizDiv;
			}
		}
		
		return null;
	}

    
    public static HttpRequest.BodyPublisher multipartToByte(Map<Object, Object> map, String boundary) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<Object, Object> data : map.entrySet()) {
            stringBuilder.setLength(0);
            stringBuilder.append(DOUBLE_HYPHEN).append(boundary).append(LINEFEED);

            if (data.getValue() instanceof Path) {
                Path filePath = (Path) data.getValue();
                String mimeType = Files.probeContentType(filePath);
                byte[] fileByte = Files.readAllBytes(filePath);

                stringBuilder.append("Content-Disposition: form-data; name=")
                        .append(QUTATE).append(data.getKey()).append(QUTATE)
                        .append("; filename= ").append(QUTATE).append(filePath.getFileName()).append(QUTATE)
                        .append(LINEFEED)
                        .append("Content-Type: ").append(mimeType)
                        .append(LINEFEED).append(LINEFEED);

                byteArrays.add(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                byteArrays.add(fileByte);
                byteArrays.add(LINEFEED.getBytes(StandardCharsets.UTF_8));
            } else {
                stringBuilder.append("Content-Disposition: form-data; name=")
                        .append(QUTATE).append(data.getKey()).append(QUTATE)
                        .append(";").append(LINEFEED).append(LINEFEED)
                        .append(data.getValue()).append(LINEFEED);

                byteArrays.add(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
            }
        }

        stringBuilder.setLength(0);
        stringBuilder.append(DOUBLE_HYPHEN).append(boundary).append(DOUBLE_HYPHEN);
        byteArrays.add(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

	
	public static String getJsonFromInfoFile(TargetFileInfo infoFile) {

    	ObjectMapper objectMapper = new ObjectMapper();
		Ini iniObject;
		String requestDataJsonString = "";
		try {
			if(infoFile.getFileExt().trim().toUpperCase().equals("INI")) { 				
				iniObject = new Ini(infoFile.getFile());
				Map<String, Map<String, String>> iniMap  
				    = iniObject.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				
				Map<String, Map<String, String>> iniResultMap = new HashMap<>();
				
				for(String sectionName : iniMap.keySet()){    			
					Map<String, String> dataMap = iniMap.get(sectionName);		
					Map<String, String> itemMap = new HashMap<>();
					
					for(String itemName : dataMap.keySet()) {
						if(itemName.equals("SYS_NAME")) {
							
						}
		 				String itemValue = new String(dataMap.get(itemName).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		 				
						// System.out.println("itemValue=[" + itemValue + "]");
						itemMap.put(CommonUtil.convertSnakeToHungarian(itemName), itemValue); 	 
					}
					
					iniResultMap.put(CommonUtil.convertSnakeToHungarian(sectionName), itemMap);
				} 	 
				requestDataJsonString = objectMapper.writeValueAsString(iniResultMap);
			}else if(infoFile.getFileExt().trim().toUpperCase().equals("JSON")) { 	
				Path p = Path.of(infoFile.getFileFullNameWithPath());
				requestDataJsonString = Files.readString(p);				
			}
			
			 
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return requestDataJsonString;
	}

 
//	
//	
//	public static String getJsonFromInt(File iniFile) {
//
//    	ObjectMapper objectMapper = new ObjectMapper();
//		Ini iniObject;
//		String requestDataJsonString = "";
//		try {
//			iniObject = new Ini(iniFile);
//			Map<String, Map<String, String>> iniMap  
//			    = iniObject.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//			
//			Map<String, Map<String, String>> iniResultMap = new HashMap<>();
//			for(String sectionName : iniMap.keySet()){    			
//				Map<String, String> dataMap = iniMap.get(sectionName);
//	
//				Map<String, String> itemMap = new HashMap<>();
//				for(String itemName : dataMap.keySet()) {
//	//				String itemValue = dataMap.get(itemName);
//	  				String itemValue1 = new String(dataMap.get(itemName).getBytes(), StandardCharsets.ISO_8859_1);
//	 				String itemValue = new String(dataMap.get(itemName).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
//	 				
//					System.out.println("itemValue=[" + itemValue + "]");
//					itemMap.put(CommonUtil.convertSnakeToHungarian(itemName), itemValue); 	 
//				}
//				
//				iniResultMap.put(CommonUtil.convertSnakeToHungarian(sectionName), itemMap);
//			} 	 
//			requestDataJsonString = objectMapper.writeValueAsString(iniResultMap);
//		} catch (InvalidFileFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		
//		return requestDataJsonString;
//	}
//
//
//    

	public static String convertSnakeToHungarian(String snakeCase) {

        if (snakeCase == null || snakeCase.isBlank())
        {
            return snakeCase;
        }

        // Remove underscores and capitalize the first letter of each word
        String[] words =  snakeCase.split("_");
        for (int i = 0; i < words.length; i++)
        {
        	if(i == 0) {
        		words[i] = words[i].toLowerCase();
        	}else {
        		words[i] = String.valueOf(words[i].toCharArray()[0]) + words[i].toLowerCase().substring(1);
        	}
        }
         
        return String.join("",  words);
    }
}
