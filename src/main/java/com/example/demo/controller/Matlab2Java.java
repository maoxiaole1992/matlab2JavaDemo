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
import org.omg.CORBA.PUBLIC_MEMBER;
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
    @Value("${rms.max.allow:30}")
    private double rmsMaxAllow;
    @Value("${average.max.allow:30}")
    private double averageMaxAllow;
    @Value("${kurtosis.max.allow:2000}")
    private double kurtosisMaxAllow;
    @Value("${skewness.max.allow:30}")
    private double skewnessMaxAllow;
    @Value("${sqrt.max.allow:20}")
    private double sqrtMaxAllow;
    @Value("${pulse.max.allow:30}")
    private double pulseMaxAllow;

    @ApiOperation(value = "傅里叶变换获得频域信息")
    @GetMapping("/getFreqDomain")
    public ResultData getFreqDomain() throws MWException {
        // 采样频率
        Double fs = 1000.0;
        // 初始化matlab函数对象
        MatlabFunc func = new MatlabFunc();
        // 获取原始振动数据
        Double[] dataArr = null;
        if (null == scheduleTask4Data.getDataArr()) {
            dataArr = ReadTxtFile.getContent();
        } else {
            dataArr = scheduleTask4Data.getDataArr().clone();
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

    @ApiOperation(value = "获取时域信息")
    @GetMapping("/getTimeDomain")
    public ResultData getTimeDomain() {
        try {
            Double[] dataArr = null;
            if (null == scheduleTask4Data.getDataArr()) {
                dataArr = ReadTxtFile.getContent();
            } else {
                dataArr = scheduleTask4Data.getDataArr().clone();
            }
            int[] timeArr = new int[dataArr.length];
            for (int i = 0; i < dataArr.length; i++) {
                timeArr[i] = i;
            }
            // 返回结果
            Map map = new HashMap();
            map.put("time", timeArr);
            map.put("amp", dataArr);
            return ResultData.SUCCESS(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultData.FAILURE("获取时域信息过程异常！");
        }
    }

    @ApiOperation(value = "获取时域监测指标：峰值、均方根值、平均幅值、峭度")
    @GetMapping(value = "/getTimeParams")
    public ResultData getTimeParams() {
        // 获取原始振动数据
        Double[] dataArr = null;
        if (null == scheduleTask4Data.getDataArr()) {
            dataArr = ReadTxtFile.getContent();
        } else {
            dataArr = scheduleTask4Data.getDataArr().clone();
        }
        // 1、计算峰值
        Arrays.sort(dataArr);
        // 排序之后最大绝对值在数组第一个和最后一个之间产生
        double absMaxAmp = Math.abs(dataArr[0]);
        if (absMaxAmp < Math.abs(dataArr[dataArr.length - 1])) {
            absMaxAmp = Math.abs(dataArr[dataArr.length - 1]);
        }
        // 2、获取均方根值
        double squareSum = calculateSum(dataArr, Constants.SUM_SQUARE_TYPE);
        // 3、获取平均幅值
        double absSum = calculateSum(dataArr, Constants.SUM_ABS_TYPE);
        // 4、获取峭度值
        double kurtosisSum = calculateSum(dataArr, Constants.SUM_KURTOSIS_TYPE);
        // 5、获取歪度值
        double skewnessSum = calculateSum(dataArr, Constants.SUM_SKEWNESS_TYPE);
        // 6、获取方根幅值
        double sqrtSum = calculateSum(dataArr, Constants.SUM_SQRT_TYPE);

        // 返回结果
        Map map = new HashMap();
        map.put("ampMaxReal", String.format("%.2f", absMaxAmp));
        map.put("ampMaxAllow", ampMaxAllow);
        map.put("ampRmsReal", String.format("%.2f", Math.sqrt(squareSum / dataArr.length)));
        map.put("ampRmsAllow", rmsMaxAllow);
        map.put("ampAverageReal", String.format("%.2f", absSum / dataArr.length));
        map.put("ampAverageAllow", averageMaxAllow);
        map.put("ampKurtosisReal", String.format("%.2f", kurtosisSum / dataArr.length));
        map.put("ampKurtosisAllow", kurtosisMaxAllow);
        map.put("ampSkewnessReal", String.format("%.2f", skewnessSum / dataArr.length));
        map.put("ampSkewnessAllow", skewnessMaxAllow);
        map.put("ampSqrtReal", String.format("%.2f", Math.pow(sqrtSum / dataArr.length, 2)));
        map.put("ampSqrtAllow", sqrtMaxAllow);
        // 7、脉冲指标
        map.put("ampPulseReal", String.format("%.2f", absMaxAmp / (absSum / dataArr.length)));
        map.put("ampPulseAllow", pulseMaxAllow);
        return ResultData.SUCCESS(map);
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
        // 求三次方和
        else if (Constants.SUM_SKEWNESS_TYPE.equals(type)) {
            for (double num : dataArr) {
                sum += Math.pow(num, 3);
            }
        }
        // 求二分之一次方和
        else if (Constants.SUM_SQRT_TYPE.equals(type)) {
            for (double num : dataArr) {
                sum += Math.sqrt(Math.abs(num));
            }
        }
        return sum;
    }


}

