package com.zsq.winter.es.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ObjectUtils;

/**
 * Banner创建器
 *
 * <p>实现{@link ApplicationRunner}接口，在Spring Boot应用启动完成后立即执行。
 * 用于在控制台输出加密模块的ASCII艺术Banner，包含版本信息和相关链接。</p>
 *
 * <p>功能特性：</p>
 * <ul>
 *   <li>在应用启动后自动执行</li>
 *   <li>根据配置决定是否显示Banner</li>
 *   <li>显示模块版本信息</li>
 *   <li>显示开发文档和代码仓库链接</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>项目启动后的准备工作</li>
 *   <li>加载配置文件</li>
 *   <li>执行初始化逻辑</li>
 *   <li>启动定时任务等</li>
 * </ul>
 *
 * @author dadandiaoming
 * @see ApplicationRunner
 * @see EsConfigProperties
 * @since 1.0.0
 */
@Slf4j
public class BannerCreator implements ApplicationRunner {

    /**
     * 加密配置属性
     */
    private final EsConfigProperties esConfigProperties;

    /**
     * 构造函数
     *
     * <p>通过依赖注入获取加密配置属性。</p>
     *
     * @param EsConfigProperties 加密配置属性
     */
    public BannerCreator(EsConfigProperties EsConfigProperties) {
        this.esConfigProperties = EsConfigProperties;
    }

    /**
     * 应用启动后执行的方法
     *
     * <p>在Spring Boot应用启动完成后立即执行，用于输出加密模块的Banner信息。
     * Banner包含ASCII艺术形式的模块名称、版本信息、开发文档链接和代码仓库链接。</p>
     *
     * @param args 应用启动参数
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // ASCII艺术Banner，来源：https://www.bootschool.net/ascii
        String str = "" +
                " ___       __   ___  ________   _________  _______   ________                 _______   ________      \n" +
                "|\\  \\     |\\  \\|\\  \\|\\   ___  \\|\\___   ___\\\\  ___ \\ |\\   __  \\               |\\  ___ \\ |\\   ____\\     \n" +
                "\\ \\  \\    \\ \\  \\ \\  \\ \\  \\\\ \\  \\|___ \\  \\_\\ \\   __/|\\ \\  \\|\\  \\  ____________\\ \\   __/|\\ \\  \\___|_    \n" +
                " \\ \\  \\  __\\ \\  \\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\ \\ \\  \\_|/_\\ \\   _  _\\|\\____________\\ \\  \\_|/_\\ \\_____  \\   \n" +
                "  \\ \\  \\|\\__\\_\\  \\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\ \\ \\  \\_|\\ \\ \\  \\\\  \\\\|____________|\\ \\  \\_|\\ \\|____|\\  \\  \n" +
                "   \\ \\____________\\ \\__\\ \\__\\\\ \\__\\   \\ \\__\\ \\ \\_______\\ \\__\\\\ _\\               \\ \\_______\\____\\_\\  \\ \n" +
                "    \\|____________|\\|__|\\|__| \\|__|    \\|__|  \\|_______|\\|__|\\|__|               \\|_______|\\_________\\\n" +
                "                                                                                          \\|_________|\n"
                + "\r\n版本: " + EsConstants.VERSION_NO
                + "\r\n开发文档: " + EsConstants.DEV_DOC_URL
                + "\r\nGitHub: " + EsConstants.GITHUB_URL;

        // 如果Gitee地址不为空，则添加Gitee地址
        if (EsConstants.GITEE_URL != null && !EsConstants.GITEE_URL.isEmpty()) {
            str += "\r\nGitee: " + EsConstants.GITEE_URL;
        }

        // 根据配置决定是否打印Banner
        if (!ObjectUtils.isEmpty(esConfigProperties.getIsPrint()) && esConfigProperties.getIsPrint()) {
            log.info(str);
        }
    }
}

