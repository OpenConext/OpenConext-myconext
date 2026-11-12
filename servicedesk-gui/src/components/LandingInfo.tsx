import { sanitizeHtml } from '@myconext/shared-gui';
import { Badge } from '@surfnet/curve-react';

import badge from '../icons/chatgpt/badge.webp';
import desk from '../icons/chatgpt/desk.webp';
import screen from '../icons/chatgpt/screen.webp';
import I18n from '../locale/I18n';
import './LandingInfo.scss';

type InfoBlock = [title: string, html: string, adminFunction: boolean];

const logos = [screen, desk, badge];

export const LandingInfo = () => {
    const infoBlock = (info: InfoBlock, logo: string, index: number) => {
        const reversed = index % 2 === 0 ? 'reversed' : '';
        return (
            <div key={index} className={`mod-login info ${reversed}`}>
                <div className="header-left info">
                    <div className="info-title">
                        <h3>{info[0]}</h3>
                        <div className="admin-function-container">
                            <Badge variant="info">
                                {I18n.t(
                                    `landing.${info[2] ? 'adminFunction' : 'studentFunction'}`,
                                )}
                            </Badge>
                        </div>
                    </div>
                    <p
                        dangerouslySetInnerHTML={{
                            __html: sanitizeHtml(info[1]),
                        }}
                    />
                </div>
                <div className="header-right info">
                    <img src={logo} alt="logo" className={reversed} />
                </div>
            </div>
        );
    };

    const infos = I18n.translations[I18n.locale].landing.info as InfoBlock[];

    return (
        <div className="mod-login-container bottom">
            <div className="mod-login bottom">
                <h2>{I18n.t('landing.works')}</h2>
                {infos.map((info, index) =>
                    infoBlock(info, logos[index], index),
                )}
                <div className="landing-footer">
                    <p
                        dangerouslySetInnerHTML={{
                            __html: sanitizeHtml(I18n.t('landing.footer')),
                        }}
                    />
                </div>
            </div>
        </div>
    );
};
