package com.example.demo.controller;

import com.example.demo.utils.Constants;
import com.example.demo.utils.ReadTxtFile;
import com.example.demo.utils.ResultData;
import com.example.demo.utils.ScheduleTask4Data;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import matlab2java.MatlabFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.ls.LSOutput;

import java.util.*;

/**
 * @Author: gyzhang
 * @Date: 2020/10/10 17:43
 */
@RestController
public class Matlab2Java {

    @Autowired
    private ScheduleTask4Data scheduleTask4Data;
    @Value("${amp.max.allow:30}")
    private double ampMaxAllow;

    @ApiOperation(value = "傅里叶变换获得频域信息")
    @GetMapping("/getFreqDomain")
    public ResultData getFreqDomain() throws MWException {
        // 采样频率
        Double fs = 1000.0;
        // 初始化matlab函数对象
        MatlabFunc func = new MatlabFunc();
        // 获取原始振动数据
        Double[] dataArr = scheduleTask4Data.getDataArr();
        if (null == dataArr) {
            dataArr = ReadTxtFile.getContent();
        }
        try {
            // 数组转换成matlab需要的矩阵
            int[] dims = {dataArr.length, 1};
            MWNumericArray numericArray = MWNumericArray.newInstance(dims, dataArr, MWClassID.DOUBLE);
            Object[] result = func.matlab2java(2, numericArray, fs);
            // matlab 函数返回的第一个参数  频率
            MWNumericArray mwFrequencyArr = (MWNumericArray) result[0];
            // matlab 函数返回的第二个参数  幅值
            MWNumericArray mwAmplitudeArr = (MWNumericArray) result[1];
            // 返回结果
            Map map = new HashMap();
            map.put("freq", mwFrequencyArr.getDoubleData());
            map.put("amp", mwAmplitudeArr.getDoubleData());

            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("数据计算过程异常！");
        }
    }

    @ApiOperation(value = "获取峰值（绝对值）")
    @GetMapping("/getAbsMaxAmp")
    public ResultData getAbsMaxAmp() {
        try {
            // 获取原始振动数据
            Double[] dataArr = scheduleTask4Data.getDataArr();
            if (null == dataArr) {
                dataArr = ReadTxtFile.getContent();
            }
            Arrays.sort(dataArr);
            // 排序之后最大绝对值在数组第一个和最后一个之间产生
            double maxData = Math.abs(dataArr[0]);
            if (maxData < Math.abs(dataArr[dataArr.length - 1])) {
                maxData = Math.abs(dataArr[dataArr.length - 1]);
            }
            // 返回结果
            Map map = new HashMap();
            map.put("ampMaxReal", maxData);
            map.put("ampMaxAllow", ampMaxAllow);
            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("获取峰值过程异常！");
        }
    }

    @ApiOperation(value = "获取均方根值")
    @GetMapping(value = "/getRmsData")
    public ResultData getRmsData() {
        try {
            // 获取原始振动数据
            Double[] dataArr = scheduleTask4Data.getDataArr();
            if (null == dataArr) {
                dataArr = ReadTxtFile.getContent();
            }
            // 求平方和
            double sum = calculateSum(dataArr, Constants.SUM_SQUARE_TYPE);
            // 返回结果
            Map map = new HashMap();
            map.put("ampRmsReal", String.format("%.2f", Math.sqrt(sum / dataArr.length)));
            map.put("ampRmsAllow", ampMaxAllow);
            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("获取均方根值过程异常！");
        }
    }

    @ApiOperation(value = "获取平均幅值（绝对值）")
    @GetMapping(value = "/getAbsAverageAmp")
    public ResultData getAbsAverageAmp() {
        try {
            // 获取原始振动数据
            Double[] dataArr = scheduleTask4Data.getDataArr();
            if (null == dataArr) {
                dataArr = ReadTxtFile.getContent();
            }
            // 求绝对值的和
            double sum = calculateSum(dataArr, Constants.SUM_ABS_TYPE);
            // 返回结果
            Map map = new HashMap();
            map.put("ampAverageReal", sum / dataArr.length);
            map.put("ampAverageAllow", ampMaxAllow);
            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("获取平均幅值过程异常！");
        }
    }

    @ApiOperation(value = "获取峭度值")
    @GetMapping(value = "/getKurtosisData")
    public ResultData getKurtosisData() {
        try {
            // 获取原始振动数据
            Double[] dataArr = scheduleTask4Data.getDataArr();
            if (null == dataArr) {
                dataArr = ReadTxtFile.getContent();
            }
            // 求绝对值的和
            double sum = calculateSum(dataArr, Constants.SUM_KURTOSIS_TYPE);
            // 返回结果
            Map map = new HashMap();
            map.put("ampKurtosisReal", sum / dataArr.length);
            map.put("ampKurtosisAllow", Math.pow(ampMaxAllow, 4));
            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("获取峭度值过程异常！");
        }
    }

    /**
     * 数组循环计算
     * @param dataArr
     * @return
     */
    private double calculateSum(Double[] dataArr, String type) {
        double sum = 0;
        // 求和
        if (Constants.SUM_ABS_TYPE.equals(type)) {
            for (double num : dataArr) {
                sum += Math.abs(num);
            }
        }
        // 求平方和
        else if (Constants.SUM_SQUARE_TYPE.equals(type)) {
            for (double num : dataArr) {
                sum += Math.pow(num, 2);
            }
        }
        // 求四次方和
        else if (Constants.SUM_KURTOSIS_TYPE.equals(type)) {
            for (double num : dataArr) {
                sum += Math.pow(num, 4);
            }
        }
        return sum;
    }


}

