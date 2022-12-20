import React, { useState } from 'react';
import {
    DesktopOutlined,
    BookOutlined,
    UserOutlined,
    LogoutOutlined,
    HomeOutlined,
} from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import { useAuth } from '../lib/authenticationprovider';
import Link from 'next/link';
const { Content, Footer, Sider } = Layout;

export const siteTitle = 'Apollo Tools - Resource Manager'

const Sidebar = ({ children }) => {
    const {logout} = useAuth();
    const [collapsed, setCollapsed] = useState(false);
    const [selectedKey, setSelectedKey] = useState('0');

    const onClickLogout = () => {
        logout();
    }

    return (
        <Layout className="min-h-screen"
        >
            <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
                <Menu theme="dark" defaultSelectedKeys={[selectedKey]} mode="inline" className="mt-2"
                      onClick={(e) => setSelectedKey(e.key)}
                >
                    <Menu.Item key="0">
                        <Link href="/" >
                            <HomeOutlined />
                            <span>Home</span>
                        </Link>
                    </Menu.Item>
                    <Menu.Item key="1">
                        <Link href="/resources/resources" >
                            <DesktopOutlined />
                            <span>Resources</span>
                        </Link>
                    </Menu.Item>
                    <Menu.Item key="2">
                        <Link href="/reservations/reservations">
                            <BookOutlined />
                            <span className="mr-2">Reservations</span>
                        </Link>
                    </Menu.Item>
                    <Menu.Item key="3">
                        <Link href="/accounts/profile">
                            <UserOutlined />
                            <span className="mr-2">Profile</span>
                        </Link>
                    </Menu.Item>
                    <Menu.Item key="4">
                        <div onClick={onClickLogout}>
                            <LogoutOutlined />
                            <span className="mr-2">Logout</span>
                        </div>
                    </Menu.Item>
                </Menu>
            </Sider>
            <Layout className="site-layout">
                <div
                    className="h-12 flex justify-center items-center p-0 bg-secondary"
                >
                    <div className="text-sm md:text-xl text-center font-semibold">{siteTitle}</div>
                </div>
                <Content className="ml-4">
                    <main>{children}</main>
                </Content>
                <Footer
                    className="text-center"
                >
                    Apollo Tools ©2022
                </Footer>
            </Layout>
        </Layout>
    );
};
export default Sidebar;