import { useEffect, useMemo, useState } from 'react';
import { Line } from 'react-chartjs-2';

import {
    CategoryScale,
    Chart as ChartJS,
    Filler,
    Legend,
    LineElement,
    LinearScale,
    PointElement,
    Tooltip,
} from 'chart.js';

import { Spinner } from '@surfnet/curve-react';

import { type ServiceDeskStats, getStats } from '../api';
import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import './Stats.scss';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Filler,
    Tooltip,
    Legend,
);

const BLUE = '#0041a8';
const GREEN = '#00d27a';
const GRID_LIGHT = 'rgba(0, 0, 0, 0.06)';
const GRID_DARK = 'rgba(255, 255, 255, 0.12)';

const gradient =
    (color: string) =>
    (context: { chart: ChartJS }): CanvasGradient | string => {
        const { ctx, chartArea } = context.chart;
        if (!chartArea) {
            return 'transparent';
        }
        const fill = ctx.createLinearGradient(
            0,
            chartArea.top,
            0,
            chartArea.bottom,
        );
        fill.addColorStop(0, `${color}59`);
        fill.addColorStop(1, `${color}00`);
        return fill;
    };

export const Stats = () => {
    const [stats, setStats] = useState<ServiceDeskStats>();
    const [error, setError] = useState(false);
    const dark = useAppStore((state) => state.dark);
    const GRID = dark ? GRID_DARK : GRID_LIGHT;
    const textColor = dark ? '#f5f5f4' : '#0a0a0a';

    useEffect(() => {
        const load = async () => {
            try {
                setStats(await getStats());
            } catch {
                setError(true);
            }
        };
        void load();
    }, []);

    const accounts = useMemo(() => {
        if (!stats?.firstMonth) {
            return [];
        }
        const [year, month] = stats.firstMonth.split('-').map(Number);
        const formatter = new Intl.DateTimeFormat(I18n.locale, {
            month: 'short',
            year: 'numeric',
        });
        let total = stats.baseline;
        return stats.added.map((added, index) => {
            total += added;
            return {
                month: formatter.format(new Date(year, month - 1 + index, 1)),
                added,
                total,
            };
        });
    }, [stats]);

    const accountsData = useMemo(
        () => ({
            labels: accounts.map((a) => a.month),
            datasets: [
                {
                    label: I18n.t('stats.total'),
                    data: accounts.map((a) => a.total),
                    borderColor: BLUE,
                    backgroundColor: gradient(BLUE),
                    pointBackgroundColor: BLUE,
                    fill: true,
                    tension: 0.4,
                    borderWidth: 1.5,
                    pointRadius: accounts.length > 40 ? 0 : 4,
                },
                {
                    label: I18n.t('stats.added'),
                    data: accounts.map((a) => a.added),
                    borderColor: GREEN,
                    backgroundColor: gradient(GREEN),
                    pointBackgroundColor: GREEN,
                    pointStyle: 'rect' as const,
                    fill: true,
                    tension: 0.4,
                    borderWidth: 1.5,
                    pointRadius: accounts.length > 40 ? 0 : 4,
                },
            ],
        }),
        [accounts],
    );

    if (error) {
        return (
            <main className="page-main">
                <PageHeader wide>{I18n.t('navigation.stats')}</PageHeader>
                <p className="stats__empty">{I18n.t('stats.loadError')}</p>
            </main>
        );
    }
    if (!stats) {
        return (
            <div className="loading stats__loading" role="status">
                <Spinner role="presentation" aria-hidden />
                <span>{I18n.t('stats.loading')}</span>
            </div>
        );
    }

    return (
        <main className="page-main">
            <PageHeader wide>{I18n.t('navigation.stats')}</PageHeader>
            <div className="stats__content">
                <section className="stats__card">
                    <div className="stats__heading">
                        <h2>{I18n.t('stats.accountsOverTime')}</h2>
                        <ul className="stats__legend">
                            <li>
                                <span
                                    className="stats__legend-marker stats__legend-marker--circle"
                                    style={{ backgroundColor: BLUE }}
                                />
                                {I18n.t('stats.total')}
                            </li>
                            <li>
                                <span
                                    className="stats__legend-marker stats__legend-marker--square"
                                    style={{ backgroundColor: GREEN }}
                                />
                                {I18n.t('stats.added')}
                            </li>
                        </ul>
                    </div>
                    {accounts.length === 0 ? (
                        <p className="stats__empty">{I18n.t('stats.noData')}</p>
                    ) : (
                        <div className="stats__line">
                            <Line
                                data={accountsData}
                                options={{
                                    color: textColor,
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    interaction: {
                                        mode: 'index',
                                        intersect: false,
                                    },
                                    plugins: {
                                        legend: { display: false },
                                    },
                                    scales: {
                                        x: { grid: { color: GRID } },
                                        y: {
                                            beginAtZero: true,
                                            grid: { color: GRID },
                                            ticks: { precision: 0 },
                                        },
                                    },
                                }}
                            />
                        </div>
                    )}
                </section>
            </div>
        </main>
    );
};
