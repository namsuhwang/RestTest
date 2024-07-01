import java.io.File;

public class TargetFileInfo {
	String fileFullNameWithPath;	
	String fileFullName;
	File file;
	String fileName;
	String fileExt;
	String dirPath;
	
	public TargetFileInfo(String name, File f) {
		fileFullNameWithPath = f.getAbsolutePath();
		fileFullName = name;
		file = f;
		fileName = CommonUtil.getFileNameWithoutExt(name);
		fileExt = CommonUtil.getFileExt(name);
		dirPath = file.getAbsolutePath();
	}

	public String getFileFullNameWithPath() {
		return fileFullNameWithPath;
	}

	public void setFileFullNameWithPath(String fileFullNameWithPath) {
		this.fileFullNameWithPath = fileFullNameWithPath;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	
	public String getFileFullName() {
		return fileFullName;
	}

	public void setFileFullName(String fileFullName) {
		this.fileFullName = fileFullName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "TargetFileInfo [fileFullName=" + fileFullName + ", file=" + file + "]";
	}
	
	
}
