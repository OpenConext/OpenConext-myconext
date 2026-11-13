import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import {
    CaretDownIcon,
    CaretUpDownIcon,
    CheckCircleIcon,
    DeviceMobileSpeakerIcon,
    KeyIcon,
    MagnifyingGlassIcon,
    PasswordIcon,
    ProhibitIcon,
} from '@phosphor-icons/react';
import {
    Badge,
    Button,
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuRadioGroup,
    DropdownMenuRadioItem,
    DropdownMenuTrigger,
    InputGroup,
    InputGroupAddon,
    InputGroupInput,
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from '@surfnet/curve-react';

import {
    type ServiceDeskUserSearchResult,
    type UserSearchParams,
    searchUsers,
} from '../api';
import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';
import './Users.scss';

const PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;
const MFA_METHODS = ['app', 'passkey', 'password'];

type Sort = NonNullable<UserSearchParams['sort']>;
type BlockedFilter = 'all' | 'blocked' | 'notBlocked';

// the page numbers to show: always the first and last page, and the pages around the current one
const pageItems = (current: number, pageCount: number): (number | null)[] => {
    if (pageCount <= 7) {
        return Array.from({ length: pageCount }, (_, index) => index);
    }
    const pages = new Set(
        [0, pageCount - 1, current - 1, current, current + 1].filter(
            (page) => page >= 0 && page < pageCount,
        ),
    );
    const sorted = [...pages].sort((a, b) => a - b);
    return sorted.flatMap((page, index) =>
        index > 0 && page - sorted[index - 1] > 1 ? [null, page] : [page],
    );
};

const affiliationLabel = (affiliation: string) => {
    const key = affiliation.split('@')[0].toLowerCase();
    return I18n.t(`users.affiliation.${key}`, { defaultValue: key });
};

export const Users = () => {
    const navigate = useNavigate();
    const [searchText, setSearchText] = useState('');
    const [query, setQuery] = useState('');
    const [mfa, setMfa] = useState<string[]>([]);
    const [blocked, setBlocked] = useState<BlockedFilter>('all');
    const [sort, setSort] = useState<Sort>('name');
    const [direction, setDirection] = useState<'asc' | 'desc'>('asc');
    const [page, setPage] = useState(0);
    const [result, setResult] = useState<ServiceDeskUserSearchResult | null>(
        null,
    );
    const [failed, setFailed] = useState(false);
    const request = useRef(0);

    // wait with searching until the user stops typing
    useEffect(() => {
        const timeout = setTimeout(() => {
            setQuery(searchText.trim());
            setPage(0);
        }, SEARCH_DEBOUNCE_MS);
        return () => clearTimeout(timeout);
    }, [searchText]);

    useEffect(() => {
        const current = ++request.current;
        const load = async () => {
            try {
                const response = await searchUsers({
                    query,
                    mfa,
                    blocked:
                        blocked === 'all' ? undefined : blocked === 'blocked',
                    sort,
                    direction,
                    page,
                    size: PAGE_SIZE,
                });
                // ignore the response of an outdated search
                if (current === request.current) {
                    setResult(response);
                    setFailed(false);
                }
            } catch {
                if (current === request.current) {
                    setFailed(true);
                }
            }
        };
        void load();
    }, [query, mfa, blocked, sort, direction, page]);

    const toggleMfa = (method: string, checked: boolean) => {
        setMfa((methods) =>
            checked
                ? [...methods, method]
                : methods.filter((existing) => existing !== method),
        );
        setPage(0);
    };

    const changeBlocked = (value: BlockedFilter) => {
        setBlocked(value);
        setPage(0);
    };

    const sortBy = (column: Sort) => {
        if (column === sort) {
            setDirection(direction === 'asc' ? 'desc' : 'asc');
        } else {
            setSort(column);
            setDirection('asc');
        }
        setPage(0);
    };

    const sortableHead = (column: Sort, label: string) => (
        <TableHead
            aria-sort={
                sort === column
                    ? direction === 'asc'
                        ? 'ascending'
                        : 'descending'
                    : 'none'
            }
        >
            <button
                type="button"
                className="users__sort"
                onClick={() => sortBy(column)}
            >
                {label}
                <CaretUpDownIcon />
            </button>
        </TableHead>
    );

    const numberFormat = new Intl.NumberFormat(I18n.locale);
    const total = result?.total ?? 0;
    const pageCount = Math.max(1, Math.ceil(total / PAGE_SIZE));

    return (
        <main className="page-main users">
            <PageHeader wide subtitle={I18n.t('users.description')}>
                {I18n.t('users.title')}
            </PageHeader>
            <div className="users__content">
                <div className="users__filters">
                    <InputGroup className="users__search">
                        <InputGroupInput
                            type="search"
                            value={searchText}
                            aria-label={I18n.t('users.search')}
                            placeholder={I18n.t('users.search')}
                            onChange={(e) => setSearchText(e.target.value)}
                        />
                        <InputGroupAddon align="inline-end">
                            <MagnifyingGlassIcon />
                        </InputGroupAddon>
                    </InputGroup>
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={
                                <Button
                                    variant="outline"
                                    className="users__filter"
                                />
                            }
                        >
                            {I18n.t('users.mfaFilter')}
                            {mfa.length > 0 && (
                                <Badge variant="info">{mfa.length}</Badge>
                            )}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                            {MFA_METHODS.map((method) => (
                                <DropdownMenuCheckboxItem
                                    key={method}
                                    checked={mfa.includes(method)}
                                    onCheckedChange={(checked) =>
                                        toggleMfa(method, checked)
                                    }
                                >
                                    {I18n.t(`users.mfa.${method}`)}
                                </DropdownMenuCheckboxItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                    <DropdownMenu>
                        <DropdownMenuTrigger
                            render={
                                <Button
                                    variant="outline"
                                    className="users__filter"
                                />
                            }
                        >
                            {I18n.t(
                                blocked === 'all'
                                    ? 'users.allUsers'
                                    : `users.${blocked}`,
                            )}
                            <CaretDownIcon />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                            <DropdownMenuRadioGroup
                                value={blocked}
                                onValueChange={(value) =>
                                    changeBlocked(value as BlockedFilter)
                                }
                            >
                                <DropdownMenuRadioItem value="all">
                                    {I18n.t('users.allUsers')}
                                </DropdownMenuRadioItem>
                                <DropdownMenuRadioItem value="blocked">
                                    {I18n.t('users.blocked')}
                                </DropdownMenuRadioItem>
                                <DropdownMenuRadioItem value="notBlocked">
                                    {I18n.t('users.notBlocked')}
                                </DropdownMenuRadioItem>
                            </DropdownMenuRadioGroup>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>

                <TooltipProvider>
                    <Table className="users__table">
                        <TableHeader>
                            <TableRow>
                                {sortableHead(
                                    'name',
                                    I18n.t('users.columns.name'),
                                )}
                                {sortableHead(
                                    'email',
                                    I18n.t('users.columns.email'),
                                )}
                                <TableHead>
                                    {I18n.t('users.columns.mfa')}
                                </TableHead>
                                <TableHead>
                                    {I18n.t('users.columns.affiliations')}
                                </TableHead>
                                <TableHead>
                                    {I18n.t('users.columns.roles')}
                                </TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {result?.users.map((user) => (
                                <TableRow
                                    key={user.id}
                                    className="users__row"
                                    tabIndex={0}
                                    onClick={() =>
                                        navigate(`/users/${user.id}`)
                                    }
                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter') {
                                            navigate(`/users/${user.id}`);
                                        }
                                    }}
                                >
                                    <TableCell className="users__name">
                                        <Tooltip>
                                            <TooltipTrigger
                                                render={<span />}
                                                className="users__name-text"
                                            >
                                                {user.blocked && (
                                                    <ProhibitIcon
                                                        className="users__blocked"
                                                        aria-label={I18n.t(
                                                            'users.blockedUser',
                                                        )}
                                                    />
                                                )}
                                                <span>{user.name}</span>
                                            </TooltipTrigger>
                                            <TooltipContent>
                                                {user.name}
                                            </TooltipContent>
                                        </Tooltip>
                                    </TableCell>
                                    <TableCell>{user.email}</TableCell>
                                    <TableCell>
                                        {user.mfaMethods.length === 0
                                            ? '–'
                                            : user.mfaMethods.map((method) => (
                                                  <Badge
                                                      key={method}
                                                      variant="info"
                                                  >
                                                      {method === 'app' ? (
                                                          <DeviceMobileSpeakerIcon />
                                                      ) : method ===
                                                        'password' ? (
                                                          <PasswordIcon />
                                                      ) : (
                                                          <KeyIcon />
                                                      )}
                                                      {I18n.t(
                                                          `users.mfa.${method}`,
                                                      )}
                                                  </Badge>
                                              ))}
                                    </TableCell>
                                    <TableCell>
                                        {user.affiliations.length === 0
                                            ? '–'
                                            : user.affiliations.map(
                                                  (affiliation) => (
                                                      <Badge
                                                          key={affiliation}
                                                          variant="success"
                                                      >
                                                          <CheckCircleIcon />
                                                          {affiliationLabel(
                                                              affiliation,
                                                          )}
                                                      </Badge>
                                                  ),
                                              )}
                                    </TableCell>
                                    <TableCell>
                                        {user.roles.length === 0
                                            ? '–'
                                            : user.roles.map((role) => (
                                                  <Badge
                                                      key={role}
                                                      variant="info"
                                                  >
                                                      {role}
                                                  </Badge>
                                              ))}
                                    </TableCell>
                                </TableRow>
                            ))}
                            {result && result.users.length === 0 && (
                                <TableRow>
                                    <TableCell
                                        colSpan={5}
                                        className="users__empty"
                                    >
                                        {I18n.t('users.empty')}
                                    </TableCell>
                                </TableRow>
                            )}
                            {failed && (
                                <TableRow>
                                    <TableCell
                                        colSpan={5}
                                        className="users__empty"
                                    >
                                        {I18n.t('users.error')}
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TooltipProvider>

                {total > 0 && (
                    <div className="users__pagination">
                        <Pagination className="users__pages">
                            <PaginationContent>
                                <PaginationItem>
                                    <PaginationPrevious
                                        text={I18n.t('users.previous')}
                                        aria-disabled={page === 0}
                                        onClick={() =>
                                            setPage((current) =>
                                                Math.max(0, current - 1),
                                            )
                                        }
                                    />
                                </PaginationItem>
                                {pageItems(page, pageCount).map(
                                    (item, index) => (
                                        <PaginationItem
                                            key={item ?? `gap-${index}`}
                                        >
                                            {item === null ? (
                                                <PaginationEllipsis
                                                    srOnlyText={I18n.t(
                                                        'users.morePages',
                                                    )}
                                                />
                                            ) : (
                                                <PaginationLink
                                                    isActive={item === page}
                                                    onClick={() =>
                                                        setPage(item)
                                                    }
                                                >
                                                    {item + 1}
                                                </PaginationLink>
                                            )}
                                        </PaginationItem>
                                    ),
                                )}
                                <PaginationItem>
                                    <PaginationNext
                                        text={I18n.t('users.next')}
                                        aria-disabled={page >= pageCount - 1}
                                        onClick={() =>
                                            setPage((current) =>
                                                Math.min(
                                                    pageCount - 1,
                                                    current + 1,
                                                ),
                                            )
                                        }
                                    />
                                </PaginationItem>
                            </PaginationContent>
                        </Pagination>
                        <span className="users__summary">
                            {I18n.t('users.summary', {
                                from: numberFormat.format(page * PAGE_SIZE + 1),
                                to: numberFormat.format(
                                    Math.min(total, (page + 1) * PAGE_SIZE),
                                ),
                                total: numberFormat.format(total),
                            })}
                        </span>
                    </div>
                )}
            </div>
        </main>
    );
};
