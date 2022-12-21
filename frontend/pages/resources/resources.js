import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { useEffect, useState } from 'react';
import { Button, Space, Table, Modal } from 'antd';
import { listResources, deleteResource } from '../../lib/ResourceService';
import { useAuth } from '../../lib/AuthenticationProvider';
import Date from '../../components/Date';
import { DeleteOutlined, InfoCircleOutlined, ExclamationCircleFilled } from '@ant-design/icons';
import Link from 'next/link';

const {Column} = Table;
const { confirm } = Modal;

const Resources = () => {
    const {token} = useAuth();
    const [error, setError] = useState(false);
    const [resources, setResources] = useState([]);

    useEffect(() => {
        listResources(token, setResources, setError);
    }, [])

    const onClickDelete = (id) => {
        deleteResource(id, token, setError)
            .then(result => {
                if (result) {
                    setResources(resources.filter(resource => resource.resource_id !== id))
                }
            });
    }

    const showDeleteConfirm = (id) => {
        confirm({
            title: 'Confirmation',
            icon: <ExclamationCircleFilled />,
            content: 'Are you sure you want to delete this resource?',
            okText: 'Yes',
            okType: 'danger',
            cancelText: 'No',
            onOk() {
                onClickDelete(id);
            },
        });
    };

    return (
        <>
            <Head>
                <title>{`${siteTitle}: Resources`}</title>
            </Head>
            <div className="card container w-11/12 max-w-7xl p-10">
                <Table dataSource={resources} rowKey={(record) => record.resource_id}>
                    <Column title="Id" dataIndex="resource_id" key="id"
                        sorter={(a, b) => a.resource_id - b.resource_id}
                        defaultSortOrder="ascend"
                    />
                    <Column title="Type" dataIndex="resource_type" key="resource_type"
                        render={(resourceType) => resourceType.resource_type}
                        sorter={(a, b) =>
                            a.resource_type.resource_type.localeCompare(b.resource_type.resource_type)}
                    />
                    <Column title="Self managed" dataIndex="is_self_managed" key="is_self_managed"
                        render={(isSelfManaged) => isSelfManaged.toString()}
                    />
                    <Column title="Created at" dataIndex="created_at" key="created_at"
                        render={(createdAt) => <Date dateString={createdAt}/>}
                        sorter={(a, b) => a.created_at - b.created_at}
                    />
                    <Column title="Action at" key="action"
                        render={(_, record) => (
                            <Space size="middle">
                                <Link href={`/resources/${record.resource_id}`}>
                                    <Button icon={<InfoCircleOutlined />}/>
                                </Link>
                                <Button onClick={() => showDeleteConfirm(record.resource_id)} icon={<DeleteOutlined />}/>
                            </Space>
                        )}
                    />
                </Table>
            </div>
        </>
    );
}

export default Resources;