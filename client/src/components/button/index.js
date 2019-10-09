import style from './style';

const ButtonElement = ({onClick, text}) => (
    <>
        <button class={style.button} onClick={onClick}>{text}</button>
    </>
);

export default ButtonElement;
