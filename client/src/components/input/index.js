import style from './style';

const InputElement = ({onChange, value, placeholder = ""}) => (
    <>
        <input class={style.input} type="text" onChange={onChange} value={value} placeholder={placeholder}/>
    </>
);

export default InputElement;
