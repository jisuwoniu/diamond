package com.taobao.diamond.server.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.server.exception.ConfigServiceException;


/**
 * ���̲�������
 * 
 * @author boyan
 * @date 2010-5-4
 */
@Service
public class DiskService {

    private static final Log log = LogFactory.getLog(DiskService.class);

    /**
     * �޸ı�ǻ���
     */
    private final ConcurrentHashMap<String/* dataId + group */, Boolean/* �Ƿ������޸� */> modifyMarkCache =
            new ConcurrentHashMap<String, Boolean>();

    @Autowired
    private ServletContext servletContext;


    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    public ServletContext getServletContext() {
        return this.servletContext;
    }


    /**
     * ��Ԫ������
     * 
     * @return
     */
    public ConcurrentHashMap<String, Boolean> getModifyMarkCache() {
        return this.modifyMarkCache;
    }


    /**
     * ��ȡ�����ļ�·��, ��Ԫ������
     * 
     * @param dataId
     * @param group
     * @return
     * @throws FileNotFoundException
     */
    public String getFilePath(String dataId, String group) throws FileNotFoundException {
        return getFilePath(Constants.BASE_DIR + "/" + group + "/" + dataId);
    }


    public void saveToDisk(ConfigInfo configInfo) {
        String group = configInfo.getGroup();
        String dataId = configInfo.getDataId();
        String content = configInfo.getContent();
        String cacheKey = generateCacheKey(group, dataId);
        // �������д����
        if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
            File tempFile = null;
            try {
                // Ŀ��Ŀ¼
                String groupPath = getFilePath(Constants.BASE_DIR + "/" + group);
                createDirIfNessary(groupPath);
                // Ŀ���ļ�
                File targetFile = createFileIfNessary(groupPath, dataId);
                // ������ʱ�ļ�
                tempFile = createTempFile(dataId, group);
                // д��������ʱ�ļ�
                FileUtils.writeStringToFile(tempFile, content, Constants.ENCODE);
                // ����ʱ�ļ�����Ŀ���ļ�, ��ɱ��δ��̲���
                FileUtils.copyFile(tempFile, targetFile);
            }
            catch (Exception e) {
                String errorMsg = "save disk error, dataId=" + dataId + ",group=" + group;
                log.error(errorMsg, e);
                throw new ConfigServiceException(errorMsg, e);
            }
            finally {
                // ɾ����ʱ�ļ�
                if (tempFile != null && tempFile.exists()) {
                    FileUtils.deleteQuietly(tempFile);
                }
                // ������
                this.modifyMarkCache.remove(cacheKey);
            }
        }
        else {
            throw new ConfigServiceException("config info is being motified, dataId=" + dataId + ",group=" + group);
        }

    }


    public boolean isModified(String dataId, String group) {
        return this.modifyMarkCache.get(generateCacheKey(group, dataId)) != null;
    }


    /**
     * ���ɻ���key�����ڱ���ļ��Ƿ����ڱ��޸�
     * 
     * @param group
     * @param dataId
     * 
     * @return
     */
    public final String generateCacheKey(String group, String dataId) {
        return group + "/" + dataId;
    }


    public void removeConfigInfo(String dataId, String group) {
        String cacheKey = generateCacheKey(group, dataId);
        // �������д����
        if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
            try {
                String basePath = getFilePath(Constants.BASE_DIR);
                createDirIfNessary(basePath);

                String groupPath = getFilePath(Constants.BASE_DIR + "/" + group);
                File groupDir = new File(groupPath);
                if (!groupDir.exists()) {
                    return;
                }

                String dataPath = getFilePath(Constants.BASE_DIR + "/" + group + "/" + dataId);
                File dataFile = new File(dataPath);
                if (!dataFile.exists()) {
                    return;
                }

                FileUtils.deleteQuietly(dataFile);
            }
            catch (Exception e) {
                String errorMsg = "delete config info error, dataId=" + dataId + ",group=" + group;
                log.error(errorMsg, e);
                throw new ConfigServiceException(errorMsg, e);
            }
            finally {
                // ������
                this.modifyMarkCache.remove(cacheKey);
            }
        }
        else {
            throw new ConfigServiceException("config info is being motified, dataId=" + dataId + ",group=" + group);
        }
    }


    private String getFilePath(String dir) throws FileNotFoundException {
        return WebUtils.getRealPath(servletContext, dir);
    }


    private void createDirIfNessary(String path) {
        final File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    private File createFileIfNessary(String parent, String child) throws IOException {
        final File file = new File(parent, child);
        if (!file.exists()) {
            file.createNewFile();
            // �����ļ�Ȩ��
            changeFilePermission(file);
        }
        return file;
    }


    private void changeFilePermission(File file) {
        // �ļ�Ȩ������Ϊ600
        file.setExecutable(false, false);
        file.setWritable(false, false);
        file.setReadable(false, false);
        file.setExecutable(false, true);
        file.setWritable(true, true);
        file.setReadable(true, true);
    }


    private File createTempFile(String dataId, String group) throws IOException {
        return File.createTempFile(group + "-" + dataId, ".tmp");
    }

}
