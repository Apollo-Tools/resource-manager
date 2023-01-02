import { useAuth } from '../../lib/AuthenticationProvider';
import { useEffect, useState } from 'react';
import { listResources } from '../../lib/ResourceService';
import { InfoCircleOutlined } from '@ant-design/icons';
import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { Button, Table, message } from 'antd';
import Date from '../../components/Date';
import Link from 'next/link';
import { reserveResources } from '../../lib/ReservaionService';

const { Column } = Table;

const NewReservation = () => {
    const {token, checkTokenExpired} = useAuth();
    const [error, setError] = useState(false);
    const [resources, setResources] = useState([]);
    const [selectedResourceIds, setSelectedResourceIds] = useState([]);
    const [newReservation, setNewReservation] = useState();
    const [messageApi, contextHolder] = message.useMessage();

    useEffect(() => {
        if (!checkTokenExpired()) {
            listResources(token, setResources, setError);
        }
    }, [])

    const onClickReserve = () => {
        if (!checkTokenExpired()) {
            reserveResources(selectedResourceIds, token, setNewReservation, setError)
                .then(() => {
                    messageApi.open({
                        type: 'success',
                        content: `Reservation with id '${newReservation.reservation_id}' has been created!`,
                    });
                    setSelectedResourceIds([]);
                });
        }
    }

    const rowSelection = {
        selectedResourceIds,
        onChange: (newSelectedResourceIds) => {
            setSelectedResourceIds(newSelectedResourceIds)
        }
    };

    return (
        <>
            <Head>
                <title>{`${siteTitle}: Resources`}</title>
            </Head>
            <div className="card container w-11/12 max-w-7xl p-10">
                <Table dataSource={resources} rowKey={(record) => record.resource_id}
                       rowSelection={{type: "checkbox" , ...rowSelection}}
                >
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
                    <Column title="Actions" key="action"
                            render={(_, record) => (
                                <Link href={`/resources/${record.resource_id}`}>
                                    <Button icon={<InfoCircleOutlined />}/>
                                </Link>
                            )}
                    />
                </Table>
                <Button disabled={selectedResourceIds.length <= 0 } type="primary" onClick={onClickReserve} >Reserve</Button>
            </div>
        </>
    );
}

export default NewReservation;