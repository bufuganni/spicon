package app.util;

import app.conf.SerialConf;
import gnu.io.*;
import lombok.Cleanup;
import lombok.extern.apachecommons.CommonsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * @author zhong
 */
@CommonsLog
public class SerialPortUtils {
    /**
     * 获取提供服务的SerialPort 对象
     *
     * @return SerialPort
     */
    public static SerialPort getSerialPort() throws NoSuchPortException, PortInUseException {
        SerialPort serialPort;
        try {
            serialPort = SerialPortUtils.openPort(SerialConf.PORT_NAMEMAC);
        } catch (NoSuchPortException | PortInUseException e) {
            log.error("打开端口异常：" + SerialConf.PORT_NAME, e);
            throw e;
        }
        return serialPort;
    }
    /**
     * 查找所有可用端口
     *
     * @return 可用端口名称列表
     */
    public static final ArrayList<String> findPort() {
        //获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList();
        //将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;
    }

    /**
     * @param portName 端口名
     * @return 串口对象
     * @throws NoSuchPortException 没有该端口对应的串口设备
     * @throws PortInUseException  端口已被占用
     */
    public static final SerialPort openPort(String portName) throws NoSuchPortException, PortInUseException {
        // 通过端口名识别端口
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        // 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
        CommPort commPort = portIdentifier.open(portName, 2000);
        SerialPort serialPort = null;
        // 判断是不是串口
        if (commPort instanceof SerialPort) {
            serialPort = (SerialPort) commPort;
            try {
                // 设置一下串口的波特率等参数
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("",e);
            }
        }
        return serialPort;
    }

    /**
     * 关闭串口
     *
     * @param serialPort 待关闭的串口对象
     */
    public static void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    /**
     * 往串口发送数据
     *
     * @param serialPort 串口对象
     * @param order      待发送数据
     */
    public static void sendToPort(SerialPort serialPort, byte[] order) throws IOException {
        if(serialPort == null){
            return;
        }
        @Cleanup OutputStream out = serialPort.getOutputStream();
        out.write(order);
        out.flush();
    }

    /**
     * 从串口读取数据
     *
     * @param serialPort 当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    public static byte[] readFromPort(SerialPort serialPort) throws IOException {
        if(serialPort == null){
            return null;
        }
        @Cleanup InputStream in = serialPort.getInputStream();
        byte[] bytes = null;
        // 获取buffer里的数据长度
        int bufflenth = in.available();
        while (bufflenth != 0) {
            // 初始化byte数组为buffer中数据的长度
            bytes = new byte[bufflenth];
            in.read(bytes);
            bufflenth = in.available();
        }
        return bytes;
    }

    /**
     * 添加监听器
     *
     * @param port     串口对象
     * @param listener 串口监听器
     */
    public static void addListener(SerialPort port, SerialPortEventListener listener) {
        try {
            // 给串口添加监听器
            port.addEventListener(listener);
            // 设置当有数据到达时唤醒监听接收线程
            port.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
            port.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            log.error("添加事件异常：", e);
        }
    }
}
