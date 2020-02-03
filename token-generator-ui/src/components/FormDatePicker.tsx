import React, {useEffect} from 'react';
import DatePicker from "react-datepicker";

interface Props {
    value: Date;
    claimName: string;
    onChange: (field: string, value: any, shouldValidate?: boolean | undefined) => any;
}

const FormDatePicker = ({ value, claimName, onChange }: Props) => {
    useEffect(() => {
        claimName && onChange(claimName, new Date(value), true)
    }, []);

    return (
        <DatePicker
            showTimeSelect
            timeFormat="HH:mm"
            dateFormat="MMM d, yyyy h:mm aa"
            selected={value}
            onChange={newDate => onChange(claimName, newDate, true)}
        />
    )
};

export default FormDatePicker;