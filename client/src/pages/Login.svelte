<script>
    let
</script>

<div class={style.home}>
    <div class={style.card}>
        <div class={style.container}>
            <img class={style.logo} src={logo} alt="Logo"/>
            <h2>{I18n.t('login.header')}</h2>
            <InputElement placeholder={I18n.t('login.emailPlaceholder')}
                          onChange={e => this.setState({email: e.target.value})}
                          ref={this.ref}
                                  value={email}/>
        </div>
        <div class={style.buttons}>
            <ButtonElement text={I18n.t('login.next')}/>
        </div>
    </div>
</div>