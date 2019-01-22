package app.controller;

import app.conf.SerialConf;
import app.util.HexUtils;
import app.util.SerialPortUtils;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

@CommonsLog
@RestController("spc")
@Api(value = "spc", tags = {"spc"})
@RequestMapping(value = "spc")
public class SPCController {
    private SerialPort serialPort = null;

    /**
     * 初始化时打开端口
     */
    @PostConstruct
    public void init() {
        try {
            serialPort = SerialPortUtils.getSerialPort();
        } catch (NoSuchPortException | PortInUseException e) {
            log.info("打开端口失败。。。");
            return;
        }
        if (serialPort == null) {
            log.info("检查端口是否启动。。。");
        }
        this.addListener();
        log.info("端口启动完成:" + serialPort.toString());
    }

    @ApiOperation(value = "打开端口")
    @PostMapping("open")
    public String open() {
        this.init();
        return "启动成功";
    }

    @ApiOperation(value = "发送消息")
    @PostMapping("send")
    public String sendToPort(String message) {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            SerialPortUtils.sendToPort(serialPort, HexUtils.hexStringToBytes(message));
            log.info("发送消息:" + message.toString());

        } catch (IOException e) {
            log.error("发送数据异常：", e);
        }
        return "发送成功";
    }

    @ApiOperation(value = "读取信息")
    @PostMapping("read")
    public String readFromPort() {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            return new String(SerialPortUtils.readFromPort(serialPort));
        } catch (IOException e) {
            log.error("读取数据异常：", e);
        }
        return null;
    }
    
    
    
    
    /**
     * 查询pelicano是否在线
     * @return
     */
    @ApiOperation(value = "查询pelicano是否在线")
    @PostMapping("poll")
    public String poll() {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            SerialPortUtils.sendToPort(serialPort, HexUtils.hexStringToBytes(SerialConf.POLL));
            log.info("查询pelicano是否在线发送消息:" + SerialConf.POLL.toString());

        } catch (IOException e) {
            log.error("发送数据异常：", e);
        }
        return "发送成功";
    }
    
    /**
     * 复位
     * @return
     */
    @ApiOperation(value = "复位")
    @PostMapping("reset")
    public String reset() {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            SerialPortUtils.sendToPort(serialPort, HexUtils.hexStringToBytes(SerialConf.RESET));
            log.info("复位发送消息:" + SerialConf.RESET.toString());

        } catch (IOException e) {
            log.error("发送数据异常：", e);
        }
        return "发送成功";
    }
    /**
     * 3. Testsolenid-测试pelicano上面的电磁铁是否都可正常工作，包括分币器上的2个，依 次每个电磁铁响动占用 200ms.
     * @return
     */
    @ApiOperation(value = "测试pelicano上面的电磁铁是否都可正常工作")
    @PostMapping("testSolenid")
    public String testSolenid() {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            SerialPortUtils.sendToPort(serialPort, HexUtils.hexStringToBytes(SerialConf.TEST_SOLENID));
            log.info("测试pelicano上面的电磁铁是否都可正常工作发送消息:" + SerialConf.TEST_SOLENID.toString());

        } catch (IOException e) {
            log.error("发送数据异常：", e);
        }
        return "发送成功";
    }
    
    /**
     *查询pelicano的16个通道里面分别是哪个国家的什么面额的币种，并将返回的数据存在内存中，在【收币轮训指令】解析时使用
     * @return
     */
    @ApiOperation(value = "查询pelicano的16个通道里面分别是哪个国家的什么面额的币种")
    @PostMapping("requestCoinId")
    public String requestCoinId() {
        if (serialPort == null) {
            return "端口未启动，请启动。。。";
        }
        try {
            SerialPortUtils.sendToPort(serialPort, HexUtils.hexStringToBytes(SerialConf.REQUEST_COIN_ID));
            log.info("询pelicano的16个通道里面分别是哪个国家的什么面额的币种发送消息:" + SerialConf.REQUEST_COIN_ID.toString());

        } catch (IOException e) {
            log.error("发送数据异常：", e);
        }
        return "发送成功";
    }
    
    
    @ApiOperation(value = "关闭端口")
    @PostMapping("close")
    public String close() {
        if (serialPort != null) {
            SerialPortUtils.closePort(serialPort);
        }
        return "关闭成功";
    }

    /**
     * 添加事件
     */
    private void addListener() {
        SerialPortUtils.addListener(serialPort, serialPortEvent -> {
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.BI: // 10 通讯中断
                case SerialPortEvent.OE: // 7 溢位（溢出）错误
                case SerialPortEvent.FE: // 9 帧错误
                case SerialPortEvent.PE: // 8 奇偶校验错误
                case SerialPortEvent.CD: // 6 载波检测
                case SerialPortEvent.CTS: // 3 清除待发送数据
                case SerialPortEvent.DSR: // 4 待发送数据准备好了
                case SerialPortEvent.RI: // 5 振铃指示
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2
                    // 输出缓冲区已清空
                    break;
                case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
                    byte[] data = new byte[0]; // 读取数据，存入字节数组
                    try {
                        data = SerialPortUtils.readFromPort(serialPort);
                    } catch (IOException e) {
                       log.error("读取数据异常", e);
                    }
                    // 020101b80143 01060200 43 4e 30 31 30 42 93
                    System.out.println(HexUtils.bytesToHexString(data));
                    break;
            }
        });
    }
}
