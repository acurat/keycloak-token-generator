import React, {Dispatch} from 'react';
import {Formik, FormikHelpers} from "formik";
import {Button, Form} from "semantic-ui-react";
import {SupportedTypes, Tokens} from "../utils/types";
import ModalComponent from "../components/ModalComponent";
import AddClaim, {NewClaim} from "./AddClaim";
import {getDefaultValue} from "../utils/globals";

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
            exp: SupportedTypes.Number,
        }
    }
    ;

    initialValues: FormValues = {
        iss: 'token-generator',
        azp: 'your-app',
        aud: 'your-app-tests',
        sub: 'test-id',
        exp: Date.now()
    };

    validate = (values: FormValues) => {
        const errors: FormValues = {};
        Object.keys(values).forEach(key => {
            const value = values[key];
            const {claimTypes} = this.state;

        });
        return errors;
    };

    submit = async (values: FormValues, {setSubmitting}: FormikHelpers<FormValues>) => {

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
                          setValues
                      }) => (
                        <>
                            <ModalComponent open={this.state.open} onClose={this.close}>
                                <AddClaim onAdd={this.addClaim(values, setValues)}/>
                            </ModalComponent>
                            <Form onSubmit={handleSubmit}>
                                {Object.keys(values).map((key: string) => {
                                    return (<Form.Group inline key={key}>
                                        <label key={`label-${key}`} style={{width: '125px'}}>{key}</label>
                                        <Form.Input
                                            style={{ float: 'right'}}
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
                                        />
                                    </Form.Group>)
                                })}
                                <div style={{ marginTop: '30px'}}>
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
