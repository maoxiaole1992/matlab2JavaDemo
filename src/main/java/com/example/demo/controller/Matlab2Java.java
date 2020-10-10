package com.example.demo.controller;

import com.example.demo.utils.ReadTxtFile;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import matlab2java.MatlabFunc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gyzhang
 */
@RestController
public class Matlab2Java {

    @GetMapping("/index")
    public double test() throws MWException {
        // 采样频率
        Double fs = 1000.0;
        // 初始化matlab函数对象
        MatlabFunc func = new MatlabFunc();
        // 获取原始振动数据
        Double[] dataArr = ReadTxtFile.getContent();
        // 数组转换成matlab需要的矩阵
        int[] dims = {dataArr.length, 1};
        MWNumericArray numericArray = MWNumericArray.newInstance(dims, dataArr, MWClassID.DOUBLE);
        Object[] result = func.matlab2java(2, numericArray, fs);

        // matlab 函数返回的第一个参数
        MWNumericArray output = (MWNumericArray) result[0];

        System.out.println(output.getDoubleData()[5]);
        return output.getDoubleData()[5];
    }
    @GetMapping("/test123")
    public String testrtn() {
        return "zgy";
    }

//    public static void main(String[] args) throws MWException {
//        test();
//    }
}

