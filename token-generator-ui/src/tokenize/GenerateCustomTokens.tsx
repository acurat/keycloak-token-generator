import React, {Dispatch} from 'react';
import {Formik, FormikHelpers} from "formik";
import {Button, Form} from "semantic-ui-react";
import {SupportedTypes, Tokens} from "../utils/types";
import ModalComponent from "../components/ModalComponent";
import AddClaim, {NewClaim} from "./AddClaim";
import {getDefaultValue, getTommorrowsDate, isDate, isJSON, isNumber, isString} from "../utils/globals";
import "react-datepicker/dist/react-datepicker.css";
import FormDatePicker from "../components/FormDatePicker";

interface Props {
    setTokens: Dispatch<Tokens | undefined>;
    setLoading: Dispatch<boolean>;
}

interface State {
    open: boolean;
    error: string;
    claimTypes: {
        [key: string]: SupportedTypes
    }
}

interface FormValues {
    [key: string]: any
}

class GenerateCustomTokens extends React.Component<Props, State> {
    state = {
        open: false,
        error: '',
        claimTypes: {
            iss: SupportedTypes.String,
            aud: SupportedTypes.String,
            azp: SupportedTypes.String,
            sub: SupportedTypes.String,
            exp: SupportedTypes.Date,
        }
    }
    ;

    initialValues: FormValues = {
        iss: 'token-generator (can be anything)',
        azp: 'your-app (can be anything)',
        aud: 'your-app-tests (can be anything)',
        sub: 'test-id (can be anything)',
        exp: getTommorrowsDate()
    };

    validate = (values: FormValues) => {
        const errors: FormValues = {};
        Object.keys(values).forEach(key => {
            const value = values[key];
            // @ts-ignore
            const claimType = this.state.claimTypes[key];

            try {
                if (claimType === SupportedTypes.String && !isString(value)) {
                    errors[key] = 'Value should be a string';
                } else if (claimType === SupportedTypes.Number && !isNumber(value)) {
                    errors[key] = 'Value should be a number';
                } else if (claimType === SupportedTypes.Date && !isDate(value)) {
                    // errors[key] = 'Value should be a date';
                } else if (claimType === SupportedTypes.Object && !isJSON(value)) {
                    errors[key] = 'Value should be a valid JSON';
                } else {
                }
            } catch (e) {

            }
        });
        return errors;
    };

    submit = async (values: FormValues, {setSubmitting}: FormikHelpers<FormValues>) => {

        console.log("submit", values);
        const {setTokens} = this.props;
        try {
            setSubmitting(true);
            const response = await fetch(
                `/jwt`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(values)
                }
            ).then((response) => response.text());
            setTokens({accessToken: response});
        } catch (e) {
            console.error(e);
            setTokens(undefined);
            this.setState({
                error: 'Error encountered',
            });
        } finally {
            setSubmitting(false);
        }
    };

    addClaim = (values: FormValues, setValues: any) => (newValue: NewClaim) => {
        this.setState({
            claimTypes: {...this.state.claimTypes, [newValue.name]: newValue.type}
        });
        setValues({
            ...values,
            [newValue.name]: getDefaultValue(newValue.type)
        });
        this.close();
    };

    open = () => {
        this.setState({
            open: true
        });
    };

    close = () => {
        this.setState({
            open: false
        });
    };

    render() {
        return (
            <>
                <Formik
                    initialValues={this.initialValues}
                    validate={this.validate}
                    onSubmit={this.submit}
                    validateOnMount={true}
                >
                    {({
                          values,
                          errors,
                          touched,
                          handleChange,
                          handleBlur,
                          handleSubmit,
                          isSubmitting,
                          handleReset,
                          setValues,
                          setFieldValue
                      }) => (
                        <>
                            <ModalComponent open={this.state.open} onClose={this.close}>
                                <AddClaim onAdd={this.addClaim(values, setValues)}/>
                            </ModalComponent>
                            <Form onSubmit={handleSubmit}>
                                {Object.keys(values).map((key: string) => {
                                    // @ts-ignore
                                    const type = this.state.claimTypes[key];
                                    return (<Form.Group inline key={key}>
                                        <label key={`label-${key}`} style={{width: '125px'}}>{key}</label>
                                        {(type === SupportedTypes.String || type === SupportedTypes.Number) &&
                                        <Form.Input
                                            style={{float: 'right'}}
                                            key={`input-${key}`}
                                            placeholder=""
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            value={values[key]}
                                            name={key}
                                            width={13}
                                            error={
                                                errors[key] && touched[key] && {
                                                    content: errors[key],
                                                    pointing: 'above'
                                                }
                                            }
                                        />}
                                        {(type === SupportedTypes.Object) && <Form.TextArea
                                            style={{float: 'right'}}
                                            key={`input-${key}`}
                                            placeholder=""
                                            onChange={handleChange}
                                            onBlur={handleBlur}
                                            value={values[key]}
                                            name={key}
                                            width={13}
                                            error={
                                                errors[key] && touched[key] && {
                                                    content: errors[key],
                                                    pointing: 'above'
                                                }
                                            }
                                        />}
                                        {(type === SupportedTypes.Date) &&
                                        <Form.Field style={{float: 'right'}} width={13}>
                                            {/*<DatePicker*/}
                                            {/*    showTimeSelect*/}
                                            {/*    timeFormat="HH:mm"*/}
                                            {/*    dateFormat="MMM d, yyyy h:mm aa"*/}
                                            {/*    selected={values[key]}*/}
                                            {/*    onChange={newDate => setFieldValue(key, newDate)}*/}
                                            {/*/>*/}

                                            <FormDatePicker claimName={key} value={values[key]}
                                                            onChange={setFieldValue}/>
                                        </Form.Field>}

                                    </Form.Group>)
                                })}
                                <div style={{marginTop: '30px'}}>
                                    <Button type="button" onClick={this.open}>Add More Claims</Button>
                                    <Button type="button" onClick={handleReset}>Reset Claims</Button>
                                    <Button type="submit" disabled={isSubmitting} floated={"right"}>
                                        Mint it!
                                    </Button>
                                </div>
                            </Form>
                        </>
                    )}
                </Formik>
            </>
        );
    }
}

export default GenerateCustomTokens;
