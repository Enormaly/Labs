"""REST API заявлений абитуриентов для АС «Прием».

Сущность взята из модели приема: заявление (узел A1, вход I1).
Хранение в памяти процесса.
"""

from typing import Literal

from fastapi import FastAPI, HTTPException, Path, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

StudyForm = Literal["очная", "заочная"]


class ApplicationCreate(BaseModel):
    """Данные заявления при создании. Поля соответствуют формату заявления из ТТ."""

    full_name: str = Field(
        min_length=1,
        max_length=150,
        description="Фамилия, имя, отчество. Текст до 150 знаков.",
        examples=["Иванов Иван Иванович"],
    )
    specialty: str = Field(
        min_length=1,
        max_length=150,
        description="Код и наименование специальности. Текст до 150 знаков.",
        examples=["Организация перевозок и управление на транспорте"],
    )
    study_form: StudyForm = Field(
        description="Форма обучения. Значение справочника: очная или заочная.",
        examples=["очная"],
    )
    contact: str = Field(
        min_length=1,
        max_length=100,
        description="Контактные данные. Текст до 100 знаков.",
        examples=["+375291112233"],
    )


class Application(ApplicationCreate):
    """Заявление, сохраненное в системе."""

    id: int = Field(description="Идентификатор заявления.", examples=[1])


class ErrorBody(BaseModel):
    detail: str


class ApplicationStore:
    """Хранилище заявлений в памяти процесса."""

    def __init__(self) -> None:
        self._items: dict[int, Application] = {}
        self._next_id = 1
        self._seed()

    def _seed(self) -> None:
        samples = [
            ApplicationCreate(
                full_name="Иванов Иван Иванович",
                specialty="Организация перевозок и управление на транспорте",
                study_form="очная",
                contact="+375291112233",
            ),
            ApplicationCreate(
                full_name="Петрова Мария Сергеевна",
                specialty="Подвижной состав железных дорог",
                study_form="заочная",
                contact="petrova@example.com",
            ),
        ]
        for item in samples:
            self.create(item)

    def list_all(self) -> list[Application]:
        return list(self._items.values())

    def get(self, application_id: int) -> Application:
        item = self._items.get(application_id)
        if item is None:
            raise KeyError(application_id)
        return item

    def create(self, data: ApplicationCreate) -> Application:
        item = Application(id=self._next_id, **data.model_dump())
        self._items[self._next_id] = item
        self._next_id += 1
        return item

    def delete(self, application_id: int) -> Application:
        item = self._items.pop(application_id, None)
        if item is None:
            raise KeyError(application_id)
        return item


store = ApplicationStore()

app = FastAPI(
    title="АС «Прием». Заявления абитуриентов",
    version="1.0.0",
    description=(
        "Базовый веб-сервис одной сущности из модели приема абитуриентов. "
        "Заявление — вход I1 узла A1 «Принять заявление». "
        "Документация OpenAPI: /docs."
    ),
    docs_url="/docs",
    redoc_url=None,
)

ERROR_RESPONSES = {
    400: {"model": ErrorBody, "description": "Данные заявления не прошли проверку."},
    404: {"model": ErrorBody, "description": "Заявление с таким идентификатором не найдено."},
}


@app.exception_handler(RequestValidationError)
async def validation_error_handler(_request, exc: RequestValidationError) -> JSONResponse:
    parts: list[str] = []
    for err in exc.errors():
        location = [str(item) for item in err.get("loc", []) if item != "body"]
        message = err.get("msg", "ошибка")
        if location:
            parts.append(f"{'.'.join(location)}: {message}")
        else:
            parts.append(message)
    return JSONResponse(status_code=400, content={"detail": "; ".join(parts)})


def not_found(application_id: int) -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail=f"Заявление {application_id} не найдено",
    )


@app.get(
    "/api/v1/applications",
    response_model=list[Application],
    status_code=status.HTTP_200_OK,
    summary="Список заявлений",
    tags=["Заявления"],
)
def list_applications() -> list[Application]:
    return store.list_all()


@app.get(
    "/api/v1/applications/{application_id}",
    response_model=Application,
    status_code=status.HTTP_200_OK,
    summary="Заявление по идентификатору",
    tags=["Заявления"],
    responses=ERROR_RESPONSES,
)
def get_application(
    application_id: int = Path(ge=1, description="Идентификатор заявления."),
) -> Application:
    try:
        return store.get(application_id)
    except KeyError:
        raise not_found(application_id) from None


@app.post(
    "/api/v1/applications",
    response_model=Application,
    status_code=status.HTTP_201_CREATED,
    summary="Создать заявление",
    tags=["Заявления"],
    responses=ERROR_RESPONSES,
)
def create_application(data: ApplicationCreate) -> Application:
    return store.create(data)


@app.delete(
    "/api/v1/applications/{application_id}",
    response_model=Application,
    status_code=status.HTTP_200_OK,
    summary="Удалить заявление",
    tags=["Заявления"],
    responses=ERROR_RESPONSES,
)
def delete_application(
    application_id: int = Path(ge=1, description="Идентификатор заявления."),
) -> Application:
    try:
        return store.delete(application_id)
    except KeyError:
        raise not_found(application_id) from None
