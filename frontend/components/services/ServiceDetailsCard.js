import PropTypes from 'prop-types';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';


const ServiceDetailsCard = ({service}) => {
  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <TextDataDisplay label="Name" value={service.name} className="col-span-6"/>
      <TextDataDisplay label="Image" value={service.image} className="col-span-6" />
      <TextDataDisplay label="Replicas" value={service.replicas} className="col-span-6" />
      <TextDataDisplay label="CPU" value={service.cpu} className="col-span-6" />
      <TextDataDisplay label="Memory" value={service.memory} className="col-span-6" />
      <TextDataDisplay label="Ports" value={service.ports.join(' | ')} className="col-span-6" />
      <TextDataDisplay label="Service Type" value={service.service_type.name} className="col-span-6" />
      <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={service.created_at} />}
        className="col-span-6"/>
    </div>
  );
};

ServiceDetailsCard.propTypes = {
  service: PropTypes.object.isRequired,
};

export default ServiceDetailsCard;
