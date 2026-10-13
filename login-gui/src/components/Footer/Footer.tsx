import styles from './Footer.module.scss';

// Todo: make into real links
const LINKS = ['Terms of Use', 'Privacy and terms', 'About eduID'] as const;

export const Footer = () => (
    <footer className={styles.footer}>
        <div className={styles.container}>
            <nav className={styles.links} aria-label="Footer">
                {LINKS.map((label) => (
                    <a key={label} href="#" className={styles.link}>
                        {label}
                    </a>
                ))}
            </nav>
            <span className={styles.logo}>SURF</span>
        </div>
    </footer>
);
