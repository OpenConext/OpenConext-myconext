import { useEffect, useMemo, useState } from 'react';
import { Bar, Line } from 'react-chartjs-2';

import {
    BarElement,
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

import { type ServiceDeskOverview, getOverview } from '../api';
import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import './Overview.scss';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Filler,
    Tooltip,
    Legend,
);

const BLUE = '#0041a8';
const GREEN = '#00d27a';
const GRID = 'rgba(0, 0, 0, 0.06)';
const MAX_SERVICES = 15;

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

export const Overview = () => {
    const [overview, setOverview] = useState<ServiceDeskOverview>();
    const [error, setError] = useState(false);

    useEffect(() => {
        const load = async () => {
            try {
                setOverview(await getOverview());
            } catch {
                setError(true);
            }
        };
        void load();
    }, []);

    const accountsData = useMemo(() => {
        const accounts = overview?.accounts ?? [];
        return {
            labels: accounts.map((a) => a.week),
            datasets: [
                {
                    label: I18n.t('overview.total'),
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
                    label: I18n.t('overview.added'),
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
        };
    }, [overview]);

    const servicesData = useMemo(() => {
        const services = (overview?.services ?? []).slice(0, MAX_SERVICES);
        return {
            labels: services.map((s) => s.name),
            datasets: [
                {
                    label: I18n.t('overview.services'),
                    data: services.map((s) => s.users),
                    backgroundColor: BLUE,
                    borderRadius: 8,
                    barThickness: 28,
                },
            ],
        };
    }, [overview]);

    if (error) {
        return (
            <main className="page-main">
                <PageHeader wide>{I18n.t('navigation.overview')}</PageHeader>
                <p className="overview__empty">
                    {I18n.t('overview.loadError')}
                </p>
            </main>
        );
    }
    if (!overview) {
        return <Spinner className="loading" />;
    }

    const barHeight = Math.max(servicesData.labels.length, 3) * 40 + 40;

    return (
        <main className="page-main">
            <PageHeader wide>{I18n.t('navigation.overview')}</PageHeader>
            <div className="overview__content">
                <section className="overview__card">
                    <h2>{I18n.t('overview.accountsOverTime')}</h2>
                    {overview.accounts.length === 0 ? (
                        <p className="overview__empty">
                            {I18n.t('overview.noData')}
                        </p>
                    ) : (
                        <div className="overview__line">
                            <Line
                                data={accountsData}
                                options={{
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    interaction: {
                                        mode: 'index',
                                        intersect: false,
                                    },
                                    plugins: {
                                        legend: {
                                            position: 'top',
                                            align: 'start',
                                            labels: {
                                                usePointStyle: true,
                                            },
                                        },
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
                <section className="overview__card">
                    <h2>{I18n.t('overview.services')}</h2>
                    <p className="overview__subtitle">
                        {I18n.t('overview.servicesSubtitle')}
                    </p>
                    {overview.services.length === 0 ? (
                        <p className="overview__empty">
                            {I18n.t('overview.noData')}
                        </p>
                    ) : (
                        <div style={{ height: barHeight }}>
                            <Bar
                                data={servicesData}
                                options={{
                                    indexAxis: 'y',
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    plugins: {
                                        legend: { display: false },
                                    },
                                    scales: {
                                        x: {
                                            beginAtZero: true,
                                            grid: { color: GRID },
                                            ticks: { precision: 0 },
                                        },
                                        y: { grid: { display: false } },
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
