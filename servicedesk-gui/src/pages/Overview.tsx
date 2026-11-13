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

import { MagnifyingGlassIcon } from '@phosphor-icons/react';
import {
    Button,
    InputGroup,
    InputGroupAddon,
    InputGroupInput,
    Spinner,
} from '@surfnet/curve-react';

import { type ServiceDeskOverview, getOverview } from '../api';
import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
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
const GRID_LIGHT = 'rgba(0, 0, 0, 0.06)';
const GRID_DARK = 'rgba(255, 255, 255, 0.12)';
const INITIAL_SERVICES = 6;
const BAR_HEIGHT = 36;
const BAR_GAP = 12;

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

type BarValue = { text: string; label: string };

// Draws the service name inside the bar and the value right of the bar
const barLabels = {
    id: 'barLabels',
    afterDatasetsDraw(chart: ChartJS) {
        const { ctx } = chart;
        const meta = chart.getDatasetMeta(0);
        const values = (chart.options.plugins as { barValues?: BarValue[] })
            .barValues;
        if (!values) {
            return;
        }
        ctx.save();
        ctx.font = '500 14px sans-serif';
        ctx.textBaseline = 'middle';
        meta.data.forEach((bar, index) => {
            const { x, y, base } = bar.getProps(['x', 'y', 'base'], true);
            const value = values[index];
            if (!value) {
                return;
            }
            const maxWidth = Math.max(x - base - 20, 0);
            let label = value.label;
            while (
                label.length > 1 &&
                ctx.measureText(label).width > maxWidth
            ) {
                label = label.slice(0, -1);
            }
            if (label !== value.label) {
                label = `${label.slice(0, -1).trimEnd()}…`;
            }
            ctx.fillStyle = '#fff';
            ctx.textAlign = 'left';
            ctx.fillText(label, base + 10, y);
            ctx.fillStyle = String(chart.options.color ?? '#000');
            ctx.fillText(value.text, x + 10, y);
        });
        ctx.restore();
    },
};

export const Overview = () => {
    const [overview, setOverview] = useState<ServiceDeskOverview>();
    const [error, setError] = useState(false);
    const dark = useAppStore((state) => state.dark);
    const GRID = dark ? GRID_DARK : GRID_LIGHT;
    const textColor = dark ? '#f5f5f4' : '#0a0a0a';
    const [search, setSearch] = useState('');
    const [percentage, setPercentage] = useState(false);
    const [showAll, setShowAll] = useState(false);

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

    const accounts = useMemo(() => {
        if (!overview?.firstMonth) {
            return [];
        }
        const [year, month] = overview.firstMonth.split('-').map(Number);
        const formatter = new Intl.DateTimeFormat(I18n.locale, {
            month: 'short',
            year: 'numeric',
        });
        let total = overview.baseline;
        return overview.added.map((added, index) => {
            total += added;
            return {
                month: formatter.format(new Date(year, month - 1 + index, 1)),
                added,
                total,
            };
        });
    }, [overview]);

    const accountsData = useMemo(
        () => ({
            labels: accounts.map((a) => a.month),
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
        }),
        [accounts],
    );

    const totalAccounts = accounts.at(-1)?.total ?? overview?.baseline ?? 0;
    const matchingServices = useMemo(() => {
        const query = search.trim().toLowerCase();
        return (overview?.services ?? []).filter((service) =>
            service.name.toLowerCase().includes(query),
        );
    }, [overview, search]);
    const visibleServices =
        showAll || search.trim()
            ? matchingServices
            : matchingServices.slice(0, INITIAL_SERVICES);
    const hiddenServices = matchingServices.length - visibleServices.length;

    const servicesData = useMemo(() => {
        const formatter = new Intl.NumberFormat(I18n.locale);
        return {
            labels: visibleServices.map((s) => s.name),
            datasets: [
                {
                    label: I18n.t('overview.services'),
                    data: visibleServices.map((s) =>
                        percentage && totalAccounts
                            ? (s.users / totalAccounts) * 100
                            : s.users,
                    ),
                    backgroundColor: BLUE,
                    borderRadius: 8,
                    barThickness: BAR_HEIGHT,
                },
            ],
            values: visibleServices.map((s) => ({
                label: s.name,
                text:
                    percentage && totalAccounts
                        ? `${formatter.format(Math.round((s.users / totalAccounts) * 100))}%`
                        : formatter.format(s.users),
            })),
        };
    }, [visibleServices, percentage, totalAccounts]);

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

    const barHeight =
        Math.max(servicesData.labels.length, 1) * (BAR_HEIGHT + BAR_GAP) + 8;

    return (
        <main className="page-main">
            <PageHeader wide>{I18n.t('navigation.overview')}</PageHeader>
            <div className="overview__content">
                <section className="overview__card">
                    <div className="overview__heading">
                        <h2>{I18n.t('overview.accountsOverTime')}</h2>
                        <ul className="overview__legend">
                            <li>
                                <span
                                    className="overview__legend-marker overview__legend-marker--circle"
                                    style={{ backgroundColor: BLUE }}
                                />
                                {I18n.t('overview.total')}
                            </li>
                            <li>
                                <span
                                    className="overview__legend-marker overview__legend-marker--square"
                                    style={{ backgroundColor: GREEN }}
                                />
                                {I18n.t('overview.added')}
                            </li>
                        </ul>
                    </div>
                    {accounts.length === 0 ? (
                        <p className="overview__empty">
                            {I18n.t('overview.noData')}
                        </p>
                    ) : (
                        <div className="overview__line">
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
                <section className="overview__card">
                    <h2>{I18n.t('overview.services')}</h2>
                    <p className="overview__subtitle">
                        {I18n.t('overview.servicesSubtitle')}
                    </p>
                    <div className="overview__filters">
                        <InputGroup className="overview__search">
                            <InputGroupInput
                                type="search"
                                value={search}
                                aria-label={I18n.t('overview.searchServices')}
                                placeholder={I18n.t('overview.searchServices')}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                            <InputGroupAddon align="inline-end">
                                <MagnifyingGlassIcon />
                            </InputGroupAddon>
                        </InputGroup>
                        <div
                            className="overview__toggle"
                            role="group"
                            aria-label={I18n.t('overview.services')}
                        >
                            <button
                                type="button"
                                aria-pressed={!percentage}
                                className={!percentage ? 'active' : ''}
                                onClick={() => setPercentage(false)}
                            >
                                #
                            </button>
                            <button
                                type="button"
                                aria-pressed={percentage}
                                className={percentage ? 'active' : ''}
                                onClick={() => setPercentage(true)}
                            >
                                %
                            </button>
                        </div>
                    </div>
                    {search && (
                        <Button
                            variant="link"
                            className="overview__link"
                            onClick={() => setSearch('')}
                        >
                            {I18n.t('overview.clearSearch')}
                        </Button>
                    )}
                    {visibleServices.length === 0 ? (
                        <p className="overview__empty">
                            {I18n.t('overview.noData')}
                        </p>
                    ) : (
                        <div style={{ height: barHeight }}>
                            <Bar
                                data={servicesData}
                                plugins={[barLabels]}
                                options={{
                                    indexAxis: 'y',
                                    color: textColor,
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    layout: { padding: { right: 56 } },
                                    plugins: {
                                        legend: { display: false },
                                        tooltip: { enabled: false },
                                        barValues: servicesData.values,
                                    } as never,
                                    scales: {
                                        x: {
                                            display: false,
                                            beginAtZero: true,
                                            max: percentage ? 100 : undefined,
                                        },
                                        y: {
                                            display: false,
                                            grid: { display: false },
                                        },
                                    },
                                }}
                            />
                        </div>
                    )}
                    {!search.trim() &&
                        (hiddenServices > 0 || showAll) &&
                        matchingServices.length > INITIAL_SERVICES && (
                            <Button
                                variant="link"
                                className="overview__link"
                                onClick={() => setShowAll(!showAll)}
                            >
                                {showAll
                                    ? I18n.t('overview.showLess')
                                    : I18n.t('overview.showMore', {
                                          count: hiddenServices,
                                      })}
                            </Button>
                        )}
                </section>
            </div>
        </main>
    );
};
