package com.example.demo.controller;

import com.example.demo.utils.ReadTxtFile;
import com.example.demo.utils.ResultData;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import matlab2java.MatlabFunc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Author: gyzhang
 * @Date: 2020/10/10 17:43
 */
@RestController
public class Matlab2Java {

    @GetMapping("/getFreqDomain")
    public static ResultData getFreqDomain() throws MWException {
        // 采样频率
        Double fs = 1000.0;
        // 初始化matlab函数对象
        MatlabFunc func = new MatlabFunc();
        // 获取原始振动数据
        Double[] dataArr = ReadTxtFile.getContent();
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

}

