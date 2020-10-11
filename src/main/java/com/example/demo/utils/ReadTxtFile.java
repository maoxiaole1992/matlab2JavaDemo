package com.example.demo.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: gyzhang
 * @Date: 2020/10/10 17:43
 */
public class ReadTxtFile {

    private static Log logger = LogFactory.getLog(ReadTxtFile.class);

    public static final String FILE_PATH = "D:\\ProjectCode\\files\\dataset.txt";
    /**
     * 获取文本内容
     * @return
     */
    public static Double[] getContent() {
        // 返回文本内容
        List contentList = new ArrayList();
        try {
            String encoding = "GBK";
            File file = new File(FILE_PATH);
            // 判断文件是否存在
            if (file.isFile() && file.exists()) {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String lineTxt = null;
                while (null != (lineTxt = bufferedReader.readLine())) {
                    contentList.add(getDoubleNum(lineTxt));
                }
                reader.close();
            } else {
                logger.error("找不到指定的文件！");
            }
        } catch (Exception e) {
            logger.error("读取文件内容出错！");
            e.printStackTrace();
        }
        return (Double[]) contentList.toArray(new Double[contentList.size()]);
    }

    /**
     * 将科学计数法转换成数字
     * @param numStr
     * @return
     */
    public static double getDoubleNum(String numStr) throws Exception {
        if (StringUtils.isEmpty(numStr)) {
            throw new Exception("数字转换时参数不能为空！");
        }
        BigDecimal bd = new BigDecimal(numStr.trim());
        return Double.parseDouble(bd.toPlainString());
    }

}
