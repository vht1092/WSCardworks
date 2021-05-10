package com.dvnb.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.money.Monetary;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.dvnb.ReloadComponent;
import com.dvnb.SecurityUtils;
import com.dvnb.SpringConfigurationValueHelper;
import com.dvnb.SpringContextHelper;
import com.dvnb.TimeConverter;
import com.dvnb.entities.DoiSoatData;
import com.dvnb.entities.DvnbInvoiceMc;
import com.dvnb.entities.DvnbInvoiceVs;
import com.dvnb.entities.DvnbTyGia;
import com.dvnb.entities.InvoiceUpload;
import com.dvnb.services.DescriptionService;
import com.dvnb.services.DoiSoatDataService;
import com.dvnb.services.DsqtCurrencyService;
import com.dvnb.services.DvnbInvoiceMcService;
import com.dvnb.services.DvnbInvoiceUploadService;
import com.dvnb.services.DvnbInvoiceVsService;
import com.dvnb.services.TyGiaService;
import com.monitorjbl.xlsx.StreamingReader;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.sass.internal.util.StringUtil;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@ViewScope
public class DoiSoatThanhQuyetToan extends CustomComponent implements ReloadComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiSoatThanhQuyetToan.class);
	private SpringConfigurationValueHelper configurationHelper;
	private final transient DoiSoatDataService doiSoatDataService;
	private final transient DsqtCurrencyService dsqtCurrencyService;
	public static final String CAPTION = "ĐỐI SOÁT THANH QUYẾT TOÁN";
	private DataGridDoiSoatTQTComponent grid;
	private static final String CARD_TYPE = "LOẠI THẺ";
	private static final String LOAI_TIEN = "LOẠI TIỀN TQT";
	private static final String VIEW = "VIEW";
	private static final String IMPORT = "IMPORT";
	private static final String DOI_CHIEU = "ĐỐI CHIẾU";
	private static final String INPUT_FIELD = "Vui lòng chọn giá trị";
	private static final String ERROR_MESSAGE = "Lỗi ứng dụng";
	
	private transient String sUserId;
	private final ComboBox cbbCardType;
	private final ComboBox cbbLoaiTienTQT;
	private final ComboBox cbbLoaiGD;
	private DateField dfAdvDate;
	private DateField dfFromIncomingDate;
	private DateField dfToIncomingDate;
	private final OptionGroup optgrpIncomingFile;
	
	final Button btView = new Button(VIEW);
	final Button btImport = new Button(IMPORT);
	
	final Button btDoiChieu = new Button(DOI_CHIEU);
	private Window window;
	
	// Paging
	private final static int SIZE_OF_PAGE = 1000000;
	private final static int FIRST_OF_PAGE = 0;
//	private transient HorizontalLayout pagingLayout;
	
	File fileImport = null;
	final TimeConverter timeConverter = new TimeConverter();
	String fileNameImport;
	
	List<DoiSoatData> doiSoatDataList = new ArrayList<DoiSoatData>();
	private transient Page<DoiSoatData> result;
	
	private final VerticalLayout mainLayout = new VerticalLayout();
	private int id;
	
	public DoiSoatThanhQuyetToan() {
		final VerticalLayout mainLayout = new VerticalLayout();
		final VerticalLayout formLayout = new VerticalLayout();
		formLayout.setSpacing(true);
		
		final HorizontalLayout formLayout1a= new HorizontalLayout();
		formLayout1a.setSpacing(true);
		final VerticalLayout formLayout1b= new VerticalLayout();
		formLayout1b.setSpacing(true);
		final HorizontalLayout formLayout1st = new HorizontalLayout();
		formLayout1st.setSpacing(true);
		final HorizontalLayout formLayout2nd = new HorizontalLayout();
		formLayout2nd.setSpacing(true);
		final HorizontalLayout formLayout3rd = new HorizontalLayout();
		formLayout3rd.setSpacing(true);
		formLayout3rd.setMargin(new MarginInfo(true, false, false, false));
		
		window = new Window();
		window.setWidth(90f, Unit.PERCENTAGE);
		window.setHeight(70f, Unit.PERCENTAGE);
		window.center();
		window.setModal(true);
		
		mainLayout.setCaption(CAPTION);
		final SpringContextHelper helper = new SpringContextHelper(VaadinServlet.getCurrent().getServletContext());
		final DescriptionService descService = (DescriptionService) helper.getBean("descriptionService");
		configurationHelper = (SpringConfigurationValueHelper) helper.getBean("springConfigurationValueHelper");
		doiSoatDataService = (DoiSoatDataService) helper.getBean("doiSoatDataService");
		dsqtCurrencyService = (DsqtCurrencyService) helper.getBean("dsqtCurrencyService");
		this.sUserId = SecurityUtils.getUserId();
		grid = new DataGridDoiSoatTQTComponent();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(new MarginInfo(true, false, false, false));
		final FormLayout form = new FormLayout();
		form.setMargin(new MarginInfo(false, false, false, true));
		
		final Label lbAdvDate = new Label("Ngày ADV");
		dfAdvDate = new DateField();
		dfAdvDate.setDateFormat("dd/MM/yyyy");
		dfAdvDate.addValidator(new NullValidator(INPUT_FIELD, false));
		dfAdvDate.setValidationVisible(false);
		dfAdvDate.addValueChangeListener(event -> {
			String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
			configurationHelper.setNgayAdv(advDate);
		});
		
		final Label lbFromIncomingDate = new Label("Từ ngày Incoming");
		dfFromIncomingDate = new DateField();
		dfFromIncomingDate.setDateFormat("dd/MM/yyyy");
		dfFromIncomingDate.addValidator(new NullValidator(INPUT_FIELD, false));
		dfFromIncomingDate.setValidationVisible(false);
		
		final Label lbToIncomingDate = new Label("Đến ngày Incoming");
		dfToIncomingDate = new DateField();
		dfToIncomingDate.setDateFormat("dd/MM/yyyy");
		dfToIncomingDate.addValidator(new NullValidator(INPUT_FIELD, false));
		dfToIncomingDate.setValidationVisible(false);
		
		btImport.setStyleName(ValoTheme.BUTTON_PRIMARY);
		btImport.setWidth(120.0f, Unit.PIXELS);
		btImport.setIcon(FontAwesome.UPLOAD);
		
		optgrpIncomingFile = new OptionGroup();
		optgrpIncomingFile.setSizeFull();
		optgrpIncomingFile.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
//		optgrpIncomingFile.setCaption("Merchant");
		optgrpIncomingFile.setMultiSelect(true);
//		optgrpIncomingFile.addItems("00","18","01","20","28","29","19");
		
		dfToIncomingDate.addValueChangeListener(event -> {
			showListIncomingFileName(dfFromIncomingDate,dfToIncomingDate);
		});
		
//		dfFromIncomingDate.addValueChangeListener(event -> {
//			showListIncomingFileName(dfFromIncomingDate,dfToIncomingDate);
//		});
//		
		btImport.addClickListener(event -> {
			try {
				String[] arrIncomFile = optgrpIncomingFile.getValue().toString().equals("[]") ? new String[]{"All"} : optgrpIncomingFile.getValue().toString().replace("[", "").replace("]", "").replaceAll("\\s+","").split(",");
				Set<String> setIncomFile = new HashSet<>();
				Collections.addAll(setIncomFile, arrIncomFile); 
				
				List<DoiSoatData> doisoatList = doiSoatDataService.findAllByFileIncomName(setIncomFile);
				System.out.println("Tong so giao dich import: " + doisoatList.size());
				id = 0;
				
				doisoatList.forEach(doiSoatData -> {
					if(doiSoatData.getSoThe()==null) {
						return;
					}
					if(doiSoatData.getSoThe().startsWith("550796") || doiSoatData.getSoThe().startsWith("547139")) {
						id++;
						String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
						String creTms = timeConverter.getCurrentTime();
						doiSoatData.setCreTms(new BigDecimal(creTms));
						doiSoatData.setId(creTms + String.format("%07d", id));
						doiSoatData.setUsrId(sUserId);
						doiSoatData.setNgayAdv(advDate);
						
						List<Object[]> doisoatDataTemp = doiSoatDataService.findDoiSoatInfoByPanAndApvCode(doiSoatData.getPan(),doiSoatData.getMaCapPhep(),doiSoatData.getNgayGd());
						if(doisoatDataTemp.size()>0)
						{
			    			for (Object[] b : doisoatDataTemp) {

								doiSoatData.setLoaiTienNguyenTeGd(b[12]==null ? " " : b[12].toString());
								
								doiSoatData.setTenChuThe(b[1]==null ? " " : b[1].toString());
								
								doiSoatData.setTenChuTk(b[16]==null ? " " : b[16].toString());
									
								doiSoatData.setCasa(b[11]==null ? " " : b[11].toString());
									
								doiSoatData.setStatusCw(b[9]==null ? " " : b[9].toString());
								
								doiSoatData.setDvpht(b[8]==null ? " " : b[8].toString());
									
								doiSoatData.setCif(b[14]==null ? " " : b[14].toString());
								
								doiSoatData.setCrdpgm(b[15]==null ? " " : b[15].toString());
								
								doiSoatData.setTrace(b[10]==null ? " " : b[10].toString());
								
								doiSoatData.setStTrichNoKhGd(b[4] != null ? new BigDecimal(b[4].toString().replace(",", "")) : BigDecimal.ZERO);
								doiSoatData.setStgdNguyenTeGd(b[3] != null ? new BigDecimal(b[3].toString().replace(",", "")) : BigDecimal.ZERO);
								doiSoatData.setPhiRtmGd(b[7]==null ? BigDecimal.ZERO : new BigDecimal(b[7].toString()));
								doiSoatData.setPhiIsaGd(b[6]==null ? BigDecimal.ZERO : new BigDecimal(b[6].toString()));
								
								if(doiSoatData.getLtgd().equals("IDR")) {
									doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().multiply(BigDecimal.valueOf(100)));
								}
							}
						}
						else {
							doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
							doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
							doiSoatData.setPhiRtmGd(BigDecimal.ZERO);
							doiSoatData.setPhiIsaGd(BigDecimal.ZERO);
						}
						
						if(doiSoatData.getMaGd().startsWith("28") && !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
						{
							doiSoatData.setStGd(doiSoatData.getStGd().negate());
							doiSoatData.setStTqt(doiSoatData.getStTqt().negate());
							doiSoatData.setStQdVnd(doiSoatData.getStQdVnd().negate());
						}
						
						if(doiSoatData.getMaGd().startsWith("01") & doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals("C")
						|| doiSoatData.getMaGd().startsWith("00") & !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals("C")
						|| (doiSoatData.getMaGd().startsWith("00") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
						|| (doiSoatData.getMaGd().startsWith("20") && !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
						|| (doiSoatData.getMaGd().startsWith("01") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))) {
							doiSoatData.setStGd(doiSoatData.getStGd().negate());
							doiSoatData.setStTqt(doiSoatData.getStTqt().negate());
							doiSoatData.setStQdVnd(doiSoatData.getStQdVnd().negate());
							doiSoatData.setStTrichNoKhGd(doiSoatData.getStTrichNoKhGd().negate());
							doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().negate());
						}
						
//						doiSoatData.setStGd(stGd);
//						doiSoatData.setStTqt(stTqt);
//						doiSoatData.setStQdVnd(stQdVnd);
						
						doiSoatData.setStgdNguyenTeChenhLech(doiSoatData.getStGd().subtract(doiSoatData.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatData.getStgdNguyenTeGd()));
						doiSoatData.setStgdChenhLechDoTyGia(doiSoatData.getStQdVnd().subtract(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd()));
						
						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
							doiSoatData.setTyGiaTrichNo(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd().divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP));
						else
							doiSoatData.setTyGiaTrichNo(BigDecimal.ZERO);
						doiSoatData.setSoTienGdHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
						
						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
						{
							doiSoatData.setPhiIsaHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiIsaGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
						}
						else 
							doiSoatData.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
						
						doiSoatData.setVatPhiIsaHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
						
						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
							doiSoatData.setPhiRtmHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiRtmGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
						else 
							doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
						
						doiSoatData.setVatPhiRtmHoanTraTruyThu(doiSoatData.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
						doiSoatData.setTongPhiVatHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().add(doiSoatData.getVatPhiIsaHoanTraTruyThu()).add(doiSoatData.getPhiRtmHoanTraTruyThu()).add(doiSoatData.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
						doiSoatData.setTongHoanTraTruyThu(doiSoatData.getSoTienGdHoanTraTruyThu().add(doiSoatData.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
						
//						doiSoatData.setMerchantCity(); //Value with VS
						
						DoiSoatData doiSoatDataCheck = null;
						if(!((doiSoatData.getMaGd().startsWith("20") && !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" ")) 
						|| (doiSoatData.getMaGd().startsWith("00") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
						|| (doiSoatData.getMaGd().startsWith("01") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" ")))){
							doiSoatDataCheck = doiSoatDataService.findOneByCardnoAndApvcodeAndMagdAndAndReversalIdAndAdvdate(doiSoatData.getSoThe(), doiSoatData.getMaCapPhep(),doiSoatData.getMaGd(),doiSoatData.getReversalInd(),doiSoatData.getNgayAdv());
						}
						
						if(doiSoatDataCheck!=null) {
							//Accumulated value
							doiSoatDataCheck.setStGd(doiSoatDataCheck.getStGd().add(doiSoatData.getStGd()));
							doiSoatDataCheck.setStTqt(doiSoatDataCheck.getStTqt().add(doiSoatData.getStTqt()));
							doiSoatDataCheck.setStQdVnd(doiSoatDataCheck.getStQdVnd().add(doiSoatData.getStQdVnd()));
//			    			doiSoatDataCheck.setInterchange(doiSoatDataCheck.getInterchange().add(interchange));
							doiSoatDataCheck.setStgdNguyenTeChenhLech(doiSoatDataCheck.getStGd().subtract(doiSoatDataCheck.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStgdNguyenTeGd()));
							doiSoatDataCheck.setStgdChenhLechDoTyGia(doiSoatDataCheck.getStQdVnd().subtract(doiSoatDataCheck.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStTrichNoKhGd()));
//			    			doiSoatDataCheck.setSoTienGdHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
//			    			if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//			    				doiSoatDataCheck.setPhiIsaHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiIsaGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//			    			else 
//			    				doiSoatDataCheck.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
//			    			doiSoatDataCheck.setVatPhiIsaHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));	
//			    			if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//			    				doiSoatDataCheck.setPhiRtmHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiRtmGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//			    			else 
//			    				doiSoatDataCheck.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
//			    			doiSoatDataCheck.setVatPhiRtmHoanTraTruyThu(doiSoatDataCheck.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
//			    			doiSoatDataCheck.setTongPhiVatHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().add(doiSoatDataCheck.getVatPhiIsaHoanTraTruyThu()).add(doiSoatDataCheck.getPhiRtmHoanTraTruyThu()).add(doiSoatDataCheck.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
//			    			doiSoatDataCheck.setTongHoanTraTruyThu(doiSoatDataCheck.getSoTienGdHoanTraTruyThu().add(doiSoatDataCheck.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
							doiSoatDataService.create(doiSoatDataCheck);
							
							//Add new row
							doiSoatData.setStGd(BigDecimal.ZERO);
							doiSoatData.setStTqt(BigDecimal.ZERO);
							doiSoatData.setStQdVnd(BigDecimal.ZERO);
							doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
							doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
							doiSoatData.setStgdNguyenTeChenhLech(BigDecimal.ZERO);
							doiSoatData.setStgdChenhLechDoTyGia(BigDecimal.ZERO);
							
			    			doiSoatDataService.create(doiSoatData);
						} else
							doiSoatDataService.create(doiSoatData);
						
						System.out.println("STT: " + id  + ", SO_THE: " + doiSoatData.getSoThe() + ", PAN: " + doiSoatData.getPan() 
						+ ", MA_CAP_PHEP: " + doiSoatData.getMaCapPhep() + ", MA_GD: " + doiSoatData.getMaGd() + ", NGAY_GD: " + doiSoatData.getNgayGd() 
						+ ", REVERSAL_IND: " + doiSoatData.getReversalInd() + ", STATUS_CW: " + doiSoatData.getStatusCw() + ", NGAY_FILE_INCOMING: " + doiSoatData.getNgayFileIncoming()
						+ ", TEN_CHU_THE: " + doiSoatData.getTenChuThe() + ", TEN_CHU_TK: " + doiSoatData.getTenChuTk() + ", CASA: " + doiSoatData.getCasa()
						+ ", ST_GD: " + doiSoatData.getStGd() + ", ST_TQT: " + doiSoatData.getStTqt() + ", ST_QD_VND: " + doiSoatData.getStQdVnd() + ", LTGD: " + doiSoatData.getLtgd()
						+ ", LTTQT: " + doiSoatData.getLttqt() + ", INTERCHANGE: " + doiSoatData.getInterchange() + ", DVCNT: " + doiSoatData.getDvcnt()
						+ ", ISSUER_CHARGE: " + doiSoatData.getIssuerCharge() + ", MERCHANT_CITY: " + doiSoatData.getMerchantCity() + ", ST_TRICH_NO_KH_GD: " + doiSoatData.getStTrichNoKhGd()
						+ ", STGD_NGUYEN_TE_GD: " + doiSoatData.getStgdNguyenTeGd() + ", LOAI_TIEN_NGUYEN_TE_GD: " + doiSoatData.getLoaiTienNguyenTeGd() + ", PHI_ISA_GD: " + doiSoatData.getPhiIsaGd()
						+ ", PHI_RTM_GD: " + doiSoatData.getPhiRtmGd() + ", STGD_NGUYEN_TE_CHENH_LECH: " + doiSoatData.getStgdNguyenTeChenhLech() + ", STGD_CHENH_LECH_DO_TY_GIA: " + doiSoatData.getStgdChenhLechDoTyGia()
						+ ", TY_GIA_TRICH_NO: " + doiSoatData.getTyGiaTrichNo() + ", SO_TIEN_GD_HOAN_TRA_TRUY_THU: " + doiSoatData.getSoTienGdHoanTraTruyThu() + ", PHI_ISA_HOAN_TRA_TRUY_THU" + doiSoatData.getPhiIsaHoanTraTruyThu()
						+ ", VAT_PHI_ISA_HOAN_TRA_TRUY_THU: " + doiSoatData.getVatPhiIsaHoanTraTruyThu() + ", PHI_RTM_HOAN_TRA_TRUY_THU: " + doiSoatData.getPhiRtmHoanTraTruyThu() + ", VAT_PHI_RTM_HOAN_TRA_TRUY_THU: " + doiSoatData.getVatPhiRtmHoanTraTruyThu()
						+ ", TONG_PHI_VAT_HOAN_TRA_TRUY_THU: " + doiSoatData.getTongPhiVatHoanTraTruyThu() + ", TONG_HOAN_TRA_TRUY_THU: " + doiSoatData.getTongHoanTraTruyThu() + ", PHI_XU_LY_GD: " + doiSoatData.getPhiXuLyGd()
						+ ", DVPHT: " + doiSoatData.getDvpht() + ", TRACE: " + doiSoatData.getTrace() + ", MCC: " + doiSoatData.getMcc() + ", CIF: " + doiSoatData.getCif() + ", CRD_PGM: " + doiSoatData.getCrdpgm()
	   					);
					}
					
					
				});
				
//				for(DoiSoatData doiSoatData : doisoatList) {
////					DoiSoatData doiSoatData = new DoiSoatData();
//					if(doiSoatData.getSoThe()==null) {
//						continue;
//					}
//					if(doiSoatData.getSoThe().startsWith("550796") || doiSoatData.getSoThe().startsWith("547139")) {
//						id++;
//						String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
//						String creTms = timeConverter.getCurrentTime();
//						doiSoatData.setCreTms(new BigDecimal(creTms));
//						doiSoatData.setId(creTms + String.format("%07d", id));
//						doiSoatData.setUsrId(sUserId);
//						doiSoatData.setNgayAdv(advDate);
//						
//						List<Object[]> doisoatDataTemp = doiSoatDataService.findDoiSoatInfoByPanAndApvCode(doiSoatData.getPan(),doiSoatData.getMaCapPhep(),doiSoatData.getNgayGd());
//						if(doisoatDataTemp.size()>0)
//						{
//			    			for (Object[] b : doisoatDataTemp) {
//
//								doiSoatData.setLoaiTienNguyenTeGd(b[12]==null ? " " : b[12].toString());
//								
//								doiSoatData.setTenChuThe(b[1]==null ? " " : b[1].toString());
//								
//								doiSoatData.setTenChuTk(b[16]==null ? " " : b[16].toString());
//									
//								doiSoatData.setCasa(b[11]==null ? " " : b[11].toString());
//									
//								doiSoatData.setStatusCw(b[9]==null ? " " : b[9].toString());
//								
//								doiSoatData.setDvpht(b[8]==null ? " " : b[8].toString());
//									
//								doiSoatData.setCif(b[14]==null ? " " : b[14].toString());
//								
//								doiSoatData.setCrdpgm(b[15]==null ? " " : b[15].toString());
//								
//								doiSoatData.setTrace(b[10]==null ? " " : b[10].toString());
//								
//								doiSoatData.setStTrichNoKhGd(b[4] != null ? new BigDecimal(b[4].toString().replace(",", "")) : BigDecimal.ZERO);
//								doiSoatData.setStgdNguyenTeGd(b[3] != null ? new BigDecimal(b[3].toString().replace(",", "")) : BigDecimal.ZERO);
//								doiSoatData.setPhiRtmGd(b[7]==null ? BigDecimal.ZERO : new BigDecimal(b[7].toString()));
//								doiSoatData.setPhiIsaGd(b[6]==null ? BigDecimal.ZERO : new BigDecimal(b[6].toString()));
//								
//								if(doiSoatData.getLtgd().equals("IDR")) {
//									doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().multiply(BigDecimal.valueOf(100)));
//								}
//							}
//						}
//						else {
//							doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
//							doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
//							doiSoatData.setPhiRtmGd(BigDecimal.ZERO);
//							doiSoatData.setPhiIsaGd(BigDecimal.ZERO);
//						}
//						
//						if(doiSoatData.getMaGd().startsWith("28") && !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
//						{
//							doiSoatData.setStGd(doiSoatData.getStGd().negate());
//							doiSoatData.setStTqt(doiSoatData.getStTqt().negate());
//							doiSoatData.setStQdVnd(doiSoatData.getStQdVnd().negate());
//						}
//						
//						if(doiSoatData.getMaGd().startsWith("01") & doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals("C")
//						|| doiSoatData.getMaGd().startsWith("00") & !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals("C")
//						|| (doiSoatData.getMaGd().startsWith("00") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
//						|| (doiSoatData.getMaGd().startsWith("20") && !doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))
//						|| (doiSoatData.getMaGd().startsWith("01") && doiSoatData.getReversalInd().equals("R") && doiSoatData.getStatusCw().equals(" "))) {
//							doiSoatData.setStGd(doiSoatData.getStGd().negate());
//							doiSoatData.setStTqt(doiSoatData.getStTqt().negate());
//							doiSoatData.setStQdVnd(doiSoatData.getStQdVnd().negate());
//							doiSoatData.setStTrichNoKhGd(doiSoatData.getStTrichNoKhGd().negate());
//							doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().negate());
//						}
//						
////						doiSoatData.setStGd(stGd);
////						doiSoatData.setStTqt(stTqt);
////						doiSoatData.setStQdVnd(stQdVnd);
//						
//						doiSoatData.setStgdNguyenTeChenhLech(doiSoatData.getStGd().subtract(doiSoatData.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatData.getStgdNguyenTeGd()));
//						doiSoatData.setStgdChenhLechDoTyGia(doiSoatData.getStQdVnd().subtract(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd()));
//						
//						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//							doiSoatData.setTyGiaTrichNo(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd().divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP));
//						else
//							doiSoatData.setTyGiaTrichNo(BigDecimal.ZERO);
//						doiSoatData.setSoTienGdHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
//						
//						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//						{
//							doiSoatData.setPhiIsaHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiIsaGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//						}
//						else 
//							doiSoatData.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
//						
//						doiSoatData.setVatPhiIsaHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
//						
//						if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//							doiSoatData.setPhiRtmHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiRtmGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//						else 
//							doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
//						
//						doiSoatData.setVatPhiRtmHoanTraTruyThu(doiSoatData.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
//						doiSoatData.setTongPhiVatHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().add(doiSoatData.getVatPhiIsaHoanTraTruyThu()).add(doiSoatData.getPhiRtmHoanTraTruyThu()).add(doiSoatData.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
//						doiSoatData.setTongHoanTraTruyThu(doiSoatData.getSoTienGdHoanTraTruyThu().add(doiSoatData.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
//						
////						doiSoatData.setMerchantCity(); //Value with VS
//						
//						DoiSoatData doiSoatDataCheck = new DoiSoatData();
//		    			doiSoatDataCheck = doiSoatDataService.findOneByCardnoAndApvcodeAndMagdAndAndReversalIdAndAdvdate(doiSoatData.getSoThe(), doiSoatData.getMaCapPhep(),doiSoatData.getMaGd(),doiSoatData.getReversalInd(),doiSoatData.getNgayAdv());
//						if(doiSoatDataCheck!=null) {
//							//Accumulated value
//							doiSoatDataCheck.setStGd(doiSoatDataCheck.getStGd().add(doiSoatData.getStGd()));
//							doiSoatDataCheck.setStTqt(doiSoatDataCheck.getStTqt().add(doiSoatData.getStTqt()));
//							doiSoatDataCheck.setStQdVnd(doiSoatDataCheck.getStQdVnd().add(doiSoatData.getStQdVnd()));
////			    			doiSoatDataCheck.setInterchange(doiSoatDataCheck.getInterchange().add(interchange));
//							doiSoatDataCheck.setStgdNguyenTeChenhLech(doiSoatDataCheck.getStGd().subtract(doiSoatDataCheck.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStgdNguyenTeGd()));
//							doiSoatDataCheck.setStgdChenhLechDoTyGia(doiSoatDataCheck.getStQdVnd().subtract(doiSoatDataCheck.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStTrichNoKhGd()));
////			    			doiSoatDataCheck.setSoTienGdHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
////			    			if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
////			    				doiSoatDataCheck.setPhiIsaHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiIsaGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
////			    			else 
////			    				doiSoatDataCheck.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
////			    			doiSoatDataCheck.setVatPhiIsaHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));	
////			    			if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
////			    				doiSoatDataCheck.setPhiRtmHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiRtmGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
////			    			else 
////			    				doiSoatDataCheck.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
////			    			doiSoatDataCheck.setVatPhiRtmHoanTraTruyThu(doiSoatDataCheck.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
////			    			doiSoatDataCheck.setTongPhiVatHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().add(doiSoatDataCheck.getVatPhiIsaHoanTraTruyThu()).add(doiSoatDataCheck.getPhiRtmHoanTraTruyThu()).add(doiSoatDataCheck.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
////			    			doiSoatDataCheck.setTongHoanTraTruyThu(doiSoatDataCheck.getSoTienGdHoanTraTruyThu().add(doiSoatDataCheck.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
//							doiSoatDataService.create(doiSoatDataCheck);
//							
//							//Add new row
//							doiSoatData.setStGd(BigDecimal.ZERO);
//							doiSoatData.setStTqt(BigDecimal.ZERO);
//							doiSoatData.setStQdVnd(BigDecimal.ZERO);
//							doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
//							doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
//							doiSoatData.setStgdNguyenTeChenhLech(BigDecimal.ZERO);
//							doiSoatData.setStgdChenhLechDoTyGia(BigDecimal.ZERO);
//							
//			    			doiSoatDataService.create(doiSoatData);
//						} else
//							doiSoatDataService.create(doiSoatData);
//						
//						System.out.println("STT: " + id  + ", SO_THE: " + doiSoatData.getSoThe() + ", PAN: " + doiSoatData.getPan() 
//						+ ", MA_CAP_PHEP: " + doiSoatData.getMaCapPhep() + ", MA_GD: " + doiSoatData.getMaGd() + ", NGAY_GD: " + doiSoatData.getNgayGd() 
//						+ ", REVERSAL_IND: " + doiSoatData.getReversalInd() + ", STATUS_CW: " + doiSoatData.getStatusCw() + ", NGAY_FILE_INCOMING: " + doiSoatData.getNgayFileIncoming()
//						+ ", TEN_CHU_THE: " + doiSoatData.getTenChuThe() + ", TEN_CHU_TK: " + doiSoatData.getTenChuTk() + ", CASA: " + doiSoatData.getCasa()
//						+ ", ST_GD: " + doiSoatData.getStGd() + ", ST_TQT: " + doiSoatData.getStTqt() + ", ST_QD_VND: " + doiSoatData.getStQdVnd() + ", LTGD: " + doiSoatData.getLtgd()
//						+ ", LTTQT: " + doiSoatData.getLttqt() + ", INTERCHANGE: " + doiSoatData.getInterchange() + ", DVCNT: " + doiSoatData.getDvcnt()
//						+ ", ISSUER_CHARGE: " + doiSoatData.getIssuerCharge() + ", MERCHANT_CITY: " + doiSoatData.getMerchantCity() + ", ST_TRICH_NO_KH_GD: " + doiSoatData.getStTrichNoKhGd()
//						+ ", STGD_NGUYEN_TE_GD: " + doiSoatData.getStgdNguyenTeGd() + ", LOAI_TIEN_NGUYEN_TE_GD: " + doiSoatData.getLoaiTienNguyenTeGd() + ", PHI_ISA_GD: " + doiSoatData.getPhiIsaGd()
//						+ ", PHI_RTM_GD: " + doiSoatData.getPhiRtmGd() + ", STGD_NGUYEN_TE_CHENH_LECH: " + doiSoatData.getStgdNguyenTeChenhLech() + ", STGD_CHENH_LECH_DO_TY_GIA: " + doiSoatData.getStgdChenhLechDoTyGia()
//						+ ", TY_GIA_TRICH_NO: " + doiSoatData.getTyGiaTrichNo() + ", SO_TIEN_GD_HOAN_TRA_TRUY_THU: " + doiSoatData.getSoTienGdHoanTraTruyThu() + ", PHI_ISA_HOAN_TRA_TRUY_THU" + doiSoatData.getPhiIsaHoanTraTruyThu()
//						+ ", VAT_PHI_ISA_HOAN_TRA_TRUY_THU: " + doiSoatData.getVatPhiIsaHoanTraTruyThu() + ", PHI_RTM_HOAN_TRA_TRUY_THU: " + doiSoatData.getPhiRtmHoanTraTruyThu() + ", VAT_PHI_RTM_HOAN_TRA_TRUY_THU: " + doiSoatData.getVatPhiRtmHoanTraTruyThu()
//						+ ", TONG_PHI_VAT_HOAN_TRA_TRUY_THU: " + doiSoatData.getTongPhiVatHoanTraTruyThu() + ", TONG_HOAN_TRA_TRUY_THU: " + doiSoatData.getTongHoanTraTruyThu() + ", PHI_XU_LY_GD: " + doiSoatData.getPhiXuLyGd()
//						+ ", DVPHT: " + doiSoatData.getDvpht() + ", TRACE: " + doiSoatData.getTrace() + ", MCC: " + doiSoatData.getMcc() + ", CIF: " + doiSoatData.getCif() + ", CRD_PGM: " + doiSoatData.getCrdpgm()
//	   					);
//					}
//					
//					
//				}
				
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				LOGGER.error("Error: " + e.getMessage());
			}
			
			
		});
		
		final Label lbCardType = new Label(CARD_TYPE);
		cbbCardType = new ComboBox();
		cbbCardType.setNullSelectionAllowed(true);
		cbbCardType.addItems("MC","MD","VS","VSD");
		cbbCardType.addValidator(new NullValidator(INPUT_FIELD, false));
		cbbCardType.setValidationVisible(false);
		cbbCardType.addValueChangeListener(event -> {
			configurationHelper.setCardtype(cbbCardType.getValue().toString());
		});
		
		final Label lbLoaiTienTQT = new Label(LOAI_TIEN);
		cbbLoaiTienTQT = new ComboBox();
		cbbLoaiTienTQT.setNullSelectionAllowed(false);
		cbbLoaiTienTQT.addItems("All","VND","USD");
		cbbLoaiTienTQT.setValue("All");
		cbbLoaiTienTQT.addValidator(new NullValidator(INPUT_FIELD, false));
		cbbLoaiTienTQT.setValidationVisible(false);
		
		btView.setStyleName(ValoTheme.BUTTON_PRIMARY);
		btView.setWidth(120.0f, Unit.PIXELS);
		btView.setIcon(FontAwesome.EYE);
		
		btDoiChieu.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		btDoiChieu.setWidth(120.0f, Unit.PIXELS);
		btDoiChieu.setIcon(FontAwesome.ARCHIVE);
		btDoiChieu.addClickListener(eventClickBTDoiChieu());
		
		cbbLoaiGD = new ComboBox();
		cbbLoaiGD.setNullSelectionAllowed(false);
		cbbLoaiGD.addItems("All","GDTTHH","GDRTIM","GDMSFF","HTGDTT","HTGDRT","GDBCBT","GDBNBT");
		cbbLoaiGD.setItemCaption("GDTTHH", "GD thanh toán hàng hóa");
		cbbLoaiGD.setItemCaption("GDRTIM","GD rút tiền mặt");
		cbbLoaiGD.setItemCaption("GDMSFF","GD <MONEYSEND/FASTFUND>");
		cbbLoaiGD.setItemCaption("HTGDTT","Hoàn trả GD thanh toán hàng hóa");
		cbbLoaiGD.setItemCaption("HTGDRT","Hoàn trả GD rút tiền mặt ");
		cbbLoaiGD.setItemCaption("GDBCBT","GD báo có bất thường");
		cbbLoaiGD.setItemCaption("GDBNBT","GD báo nợ bất thường");
		cbbLoaiGD.setValue("All");
		
		Upload chooseFile = new Upload(null, new Upload.Receiver() {
			private static final long serialVersionUID = 1L;

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				OutputStream outputFile = null;
				try {
					if(!checkValidatorImport()) {
						return outputFile;
					}
					// TODO Auto-generated method stub
					fileNameImport = StringUtils.substringBefore(filename, ".xlsx") + "_" + timeConverter.getCurrentTime() + ".xlsx";
					
					Window confirmDialog = new Window();
					final FormLayout content = new FormLayout();
		            content.setMargin(true);
		            
		            Button bYes = new Button("OK");
					
					confirmDialog.setCaption("Dữ liệu sẽ import, vui lòng đợi trong quá trình xử lý");
					confirmDialog.setWidth(350.0f, Unit.PIXELS);
			        try {
			        	if(!filename.isEmpty()) {
			        		fileImport = new File(configurationHelper.getPathFileRoot() + "/"+ fileNameImport);
				            if(!fileImport.exists()) {
				            	fileImport.createNewFile();
				            }
							outputFile =  new FileOutputStream(fileImport);
			        	
							bYes.addClickListener(event -> {
//					        	SHOW DATA IN GRID
								grid.dataSource = null;
								try 
								{
									InputStream is = null;
									try {
										is = new FileInputStream(fileImport);
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							    	LOGGER.info("Reading file " + fileImport.getName());
//										    	XSSFWorkbook workbook = new XSSFWorkbook(is);
							    	Workbook workbook = StreamingReader.builder()
							        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
							        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
							        .open(is);  
									
							    	Sheet sheet = workbook.getSheetAt(0);
							    	
							    	LOGGER.info("Reading row in " + fileImport.getName());
							    	id = 0;
							    	
							    	for (Row row : sheet) {
							    		String cardnoVS = "";
							    		String lttqt = "";
							    		String loaiTienTQT = "";
							    		
							    		if(row.getRowNum()>0  && row.getCell(1)!=null)
							    		{
							    			switch(cbbCardType.getValue().toString()) {
							    				case "MD":
							    					String cardno = row.getCell(1)==null ? "" : row.getCell(1).getStringCellValue();
									    			if(cardno.startsWith("550796") || cardno.startsWith("547139") || cardno.startsWith("524188")) { 
									    				id++;
									    				String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
									    				String maGd = row.getCell(2)==null ? "" : row.getCell(2).getStringCellValue();
									    				String ngayGd = "";
									    				if(row.getCell(12).getCellType() == Cell.CELL_TYPE_BLANK) 
									    					ngayGd = row.getCell(12)==null ? "" : row.getCell(12).getStringCellValue().substring(0, 6);
									    				else
									    					ngayGd = row.getCell(12).getStringCellValue().isEmpty() ? "" : row.getCell(12).getStringCellValue().substring(0, 6);
									    				 
										    			String apvcode = row.getCell(24)==null ? "" : row.getCell(24).getStringCellValue().replaceAll("[\\s|\\u00A0]+", "");
										    			
										    			BigDecimal stGd = row.getCell(3)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(3).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);;
										    			BigDecimal stTqt = row.getCell(4)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);;
										    			BigDecimal stQdVnd = BigDecimal.ZERO;
										    			if(row.getCell(5).getCellType() == Cell.CELL_TYPE_BLANK) 
										    				stQdVnd = BigDecimal.ZERO;
										    			else
										    				stQdVnd = row.getCell(5)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(5).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);
										    			BigDecimal interchange = row.getCell(44)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(44).getStringCellValue().replace(",", ""));
										    			String aditionalData = row.getCell(29).getStringCellValue().toUpperCase();
										    			String ngayFileIncoming = row.getCell(0)==null ? "" : row.getCell(0).getStringCellValue().substring(row.getCell(0).getStringCellValue().lastIndexOf("TT112T0.")+8, row.getCell(0).getStringCellValue().lastIndexOf("TT112T0.")+27);
										    			System.out.println("STT: " + id + ", Rownum: " + row.getRowNum() + ", cardno: "+ cardno + ", apvcode: " + apvcode + ", stGd: " + stGd);
										    			String ltgd = row.getCell(6)==null ? "" : String.format("%03d", Integer.parseInt(row.getCell(6).getStringCellValue()));
										    			String ltgdCode = dsqtCurrencyService.findOneByCurrNum(ltgd).getCurrCode();
										    			lttqt = row.getCell(7)==null ? "" : String.format("%03d", Integer.parseInt(row.getCell(7).getStringCellValue()));
										    			String lttqtCode = dsqtCurrencyService.findOneByCurrNum(lttqt).getCurrCode();
										    			
										    			String reversalInd = "";
										    			if(StringUtils.containsIgnoreCase(aditionalData, "25007R")) {
										    				reversalInd = "R";
										    			} else
										    				reversalInd = " ";
										    			
										    			String pan = doiSoatDataService.convertPanByCardno(cardno);
										    			
										    			DoiSoatData doiSoatDataCheck = new DoiSoatData();
										    			doiSoatDataCheck = doiSoatDataService.findOneByCardnoAndApvcodeAndMagdAndAndReversalIdAndAdvdate(cardno, apvcode,maGd,reversalInd,advDate);
										    			List<Object[]> doisoatDataTemp = doiSoatDataService.findDoiSoatInfoByPanAndApvCode(pan,apvcode,ngayGd);
										    			
										    			{
											    			DoiSoatData doiSoatData = new DoiSoatData();
											    			doiSoatData.setId(timeConverter.getCurrentTime() + String.format("%07d", id));
											    			doiSoatData.setCreTms(new BigDecimal(timeConverter.getCurrentTime()));
											    			doiSoatData.setUsrId(sUserId);
											    			doiSoatData.setNgayAdv(advDate);
											    			doiSoatData.setSoThe(cardno);
											    			doiSoatData.setMaGd(maGd);
											    			doiSoatData.setNgayGd(ngayGd);
											    			doiSoatData.setNgayFileIncoming(ngayFileIncoming);
											    			doiSoatData.setLtgd(ltgdCode);
											    			doiSoatData.setLttqt(lttqtCode);
											    			doiSoatData.setMaCapPhep(apvcode);
											    			doiSoatData.setDvcnt(row.getCell(28)==null ? "" : row.getCell(28).getStringCellValue());
											    			doiSoatData.setReversalInd(reversalInd);
											    			doiSoatData.setPan(pan);
											    			
											    			if(doisoatDataTemp.size()>0)
											    			{
												    			for (Object[] b : doisoatDataTemp) {
					
											    					doiSoatData.setLoaiTienNguyenTeGd(b[12]==null ? " " : b[12].toString());
											    					
											    					doiSoatData.setTenChuThe(b[1]==null ? " " : b[1].toString());
											    					
											    					doiSoatData.setTenChuTk(b[16]==null ? " " : b[16].toString());
											    						
											    					doiSoatData.setCasa(b[11]==null ? " " : b[11].toString());
											    						
											    					doiSoatData.setStatusCw(b[9]==null ? " " : b[9].toString());
											    					
											    					doiSoatData.setDvpht(b[8]==null ? " " : b[8].toString());
											    						
											    					doiSoatData.setCif(b[14]==null ? " " : b[14].toString());
											    					
											    					doiSoatData.setCrdpgm(b[15]==null ? " " : b[15].toString());
											    					
											    					doiSoatData.setTrace(b[10]==null ? " " : b[10].toString());
											    					
										    						doiSoatData.setStTrichNoKhGd(b[4] != null ? new BigDecimal(b[4].toString().replace(",", "")) : BigDecimal.ZERO);
											    					doiSoatData.setStgdNguyenTeGd(b[3] != null ? new BigDecimal(b[3].toString().replace(",", "")) : BigDecimal.ZERO);
											    					doiSoatData.setPhiRtmGd(b[7]==null ? BigDecimal.ZERO : new BigDecimal(b[7].toString()));
											    					doiSoatData.setPhiIsaGd(b[6]==null ? BigDecimal.ZERO : new BigDecimal(b[6].toString()));
											    					
											    					if(ltgdCode.equals("IDR")) {
											    						doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().multiply(BigDecimal.valueOf(100)));
											    					}
											    				}
											    			}
											    			else {
											    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
										    					doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
										    					doiSoatData.setPhiRtmGd(BigDecimal.ZERO);
										    					doiSoatData.setPhiIsaGd(BigDecimal.ZERO);
											    			}
											    			
											    			if(maGd.startsWith("28") && !reversalInd.equals("R") && doiSoatData.getStatusCw().equals(" "))
											    			{
											    				stGd = stGd.negate();
											    				stTqt = stTqt.negate();
											    				stQdVnd = stQdVnd.negate();
											    			}
											    			
											    			if(maGd.startsWith("01") & reversalInd.equals("R") && doiSoatData.getStatusCw().equals("C")
											    			|| maGd.startsWith("00") & !reversalInd.equals("R") && doiSoatData.getStatusCw().equals("C")
									    					|| (maGd.startsWith("00") && reversalInd.equals("R") && doiSoatData.getStatusCw().equals(" "))
											    			|| (maGd.startsWith("20") && !reversalInd.equals("R") && doiSoatData.getStatusCw().equals(" "))
											    			|| (maGd.startsWith("01") && reversalInd.equals("R") && doiSoatData.getStatusCw().equals(" "))) {
											    				stGd = stGd.negate();
											    				stTqt = stTqt.negate();
											    				stQdVnd = stQdVnd.negate();
											    				doiSoatData.setStTrichNoKhGd(doiSoatData.getStTrichNoKhGd().negate());
										    					doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().negate());
											    			}
											    			
										    				doiSoatData.setStGd(stGd);
											    			doiSoatData.setStTqt(stTqt);
											    			doiSoatData.setStQdVnd(stQdVnd);
											    			
											    			doiSoatData.setInterchange(interchange);
											    			doiSoatData.setStgdNguyenTeChenhLech(stGd.subtract(doiSoatData.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatData.getStgdNguyenTeGd()));
										    				doiSoatData.setStgdChenhLechDoTyGia(stQdVnd.subtract(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd()));
										    				
											    			if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
										    					doiSoatData.setTyGiaTrichNo(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd().divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP));
										    				else
										    					doiSoatData.setTyGiaTrichNo(BigDecimal.ZERO);
										    				doiSoatData.setSoTienGdHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
										    				
										    				if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
										    				{
										    					doiSoatData.setPhiIsaHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiIsaGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
										    				}
										    				else 
										    					doiSoatData.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
										    				
										    				doiSoatData.setVatPhiIsaHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
										    				
										    				if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
										    					doiSoatData.setPhiRtmHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiRtmGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
										    				else 
										    					doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
										    				
										    				doiSoatData.setVatPhiRtmHoanTraTruyThu(doiSoatData.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
										    				doiSoatData.setTongPhiVatHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().add(doiSoatData.getVatPhiIsaHoanTraTruyThu()).add(doiSoatData.getPhiRtmHoanTraTruyThu()).add(doiSoatData.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
										    				doiSoatData.setTongHoanTraTruyThu(doiSoatData.getSoTienGdHoanTraTruyThu().add(doiSoatData.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
										    				
											    			doiSoatData.setIssuerCharge(" "); //Value with VS
											    			doiSoatData.setPhiXuLyGd(BigDecimal.ZERO); //Value with VS
			//								    			doiSoatData.setMerchantCity(); //Value with VS
											    			doiSoatData.setMcc(row.getCell(18)==null ? "" : row.getCell(18).getStringCellValue());
											    			
											    			if(doiSoatDataCheck!=null) {
											    				//Accumulated value
											    				doiSoatDataCheck.setStGd(doiSoatDataCheck.getStGd().add(stGd));
											    				doiSoatDataCheck.setStTqt(doiSoatDataCheck.getStTqt().add(stTqt));
											    				doiSoatDataCheck.setStQdVnd(doiSoatDataCheck.getStQdVnd().add(stQdVnd));
//											    				doiSoatDataCheck.setInterchange(doiSoatDataCheck.getInterchange().add(interchange));
											    				doiSoatDataCheck.setStgdNguyenTeChenhLech(doiSoatDataCheck.getStGd().subtract(doiSoatDataCheck.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStgdNguyenTeGd()));
											    				doiSoatDataCheck.setStgdChenhLechDoTyGia(doiSoatDataCheck.getStQdVnd().subtract(doiSoatDataCheck.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStTrichNoKhGd()));
//											    				doiSoatDataCheck.setSoTienGdHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
//											    				if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//											    					doiSoatDataCheck.setPhiIsaHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiIsaGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//											    				else 
//											    					doiSoatDataCheck.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
//											    				doiSoatDataCheck.setVatPhiIsaHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));	
//											    				if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
//											    					doiSoatDataCheck.setPhiRtmHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiRtmGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//											    				else 
//											    					doiSoatDataCheck.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
//											    				doiSoatDataCheck.setVatPhiRtmHoanTraTruyThu(doiSoatDataCheck.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
//											    				doiSoatDataCheck.setTongPhiVatHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().add(doiSoatDataCheck.getVatPhiIsaHoanTraTruyThu()).add(doiSoatDataCheck.getPhiRtmHoanTraTruyThu()).add(doiSoatDataCheck.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
//											    				doiSoatDataCheck.setTongHoanTraTruyThu(doiSoatDataCheck.getSoTienGdHoanTraTruyThu().add(doiSoatDataCheck.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
											    				doiSoatDataService.create(doiSoatDataCheck);
											    				
											    				//Add new row
											    				doiSoatData.setStGd(BigDecimal.ZERO);
											    				doiSoatData.setStTqt(BigDecimal.ZERO);
											    				doiSoatData.setStQdVnd(BigDecimal.ZERO);
											    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
											    				doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
											    				doiSoatData.setStgdNguyenTeChenhLech(BigDecimal.ZERO);
											    				doiSoatData.setStgdChenhLechDoTyGia(BigDecimal.ZERO);
											    				
												    			doiSoatDataService.create(doiSoatData);
											    			} else
											    				doiSoatDataService.create(doiSoatData);
										    			}
									    			
									    			}	
									    			break;
							    				case "VSD":
							    					cardnoVS = row.getCell(8)==null ? "" : row.getCell(8).getStringCellValue();
							    					lttqt = row.getCell(27)==null ? "" : row.getCell(27).getStringCellValue();
							    					loaiTienTQT = cbbLoaiTienTQT.getValue().toString();
							    					System.out.println("Card no: " + cardnoVS + ", lttqt: " + lttqt + ", loaiTienTQT: " + loaiTienTQT);
									    			if(cardnoVS.startsWith("453618") && (loaiTienTQT.equals("All") || loaiTienTQT.equals(lttqt))) { 
									    				id++;
									    				String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
									    				String maGd = row.getCell(0)==null ? "" : row.getCell(0).getStringCellValue();
									    				String ngayGd = "";
									    				if(row.getCell(38).getCellType() == Cell.CELL_TYPE_BLANK) 
									    					ngayGd = row.getCell(38)==null ? "" : "20" + String.format("%04d",Integer.valueOf(row.getCell(38).getStringCellValue()));
									    				else
									    					ngayGd = row.getCell(38).getStringCellValue().isEmpty() ? "" : "20" + String.format("%04d",Integer.valueOf(row.getCell(38).getStringCellValue()));
									    				 
										    			String apvcode = row.getCell(43)==null ? "" : row.getCell(43).getStringCellValue().replaceAll("[\\s|\\u00A0]+", "");
										    			
										    			BigDecimal stGd = row.getCell(12)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(12).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);
										    			BigDecimal stQdVnd = BigDecimal.ZERO;
										    			if(row.getCell(10).getCellType() == Cell.CELL_TYPE_BLANK) 
										    				stQdVnd = BigDecimal.ZERO;
										    			else
										    				stQdVnd = row.getCell(10)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(10).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);
										    			BigDecimal interchange = row.getCell(28)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(28).getStringCellValue().replace(",", ""));
										    			System.out.println("STT: " + id + ", Rownum: " + row.getRowNum() + ", cardno: "+ cardnoVS + ", apvcode: " + apvcode + ", stGd: " + stGd);
										    			String ltgd = row.getCell(13)==null ? "" : row.getCell(13).getStringCellValue();
										    			String issuerCharge = row.getCell(49)==null ? "" : row.getCell(49).getStringCellValue();
										    			String ngayFileIncoming = filename.substring(filename.lastIndexOf("INCTF.")+6, filename.lastIndexOf("INCTF.")+20);
										    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
										    			Date ngayFileIncomingConvers = sdf.parse(ngayFileIncoming);
										    			sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
										    			ngayFileIncoming = sdf.format(ngayFileIncomingConvers);
										    			
										    			String pan = doiSoatDataService.convertPanByCardno(cardnoVS);
										    			
										    			List<Object[]> doisoatDataTemp = doiSoatDataService.findDoiSoatInfoByPanAndApvCode(pan,apvcode,ngayGd);
										    			
										    			DoiSoatData doiSoatData = new DoiSoatData();
										    			doiSoatData.setId(timeConverter.getCurrentTime() + String.format("%07d", id));
										    			doiSoatData.setCreTms(new BigDecimal(timeConverter.getCurrentTime()));
										    			doiSoatData.setUsrId(sUserId);
										    			doiSoatData.setNgayAdv(advDate);
										    			doiSoatData.setSoThe(cardnoVS);
										    			doiSoatData.setMaGd(maGd);
										    			doiSoatData.setNgayGd(ngayGd);
										    			doiSoatData.setNgayFileIncoming(ngayFileIncoming);
										    			doiSoatData.setLtgd(ltgd);
										    			doiSoatData.setLttqt(lttqt);
										    			doiSoatData.setMaCapPhep(apvcode);
										    			doiSoatData.setDvcnt(row.getCell(21)==null ? "" : row.getCell(21).getStringCellValue());
										    			doiSoatData.setReversalInd(" ");
										    			doiSoatData.setPan(pan);
										    			doiSoatData.setIssuerCharge(issuerCharge); //Value with VS
										    			if(issuerCharge.equals("S"))
										    				doiSoatData.setPhiXuLyGd(stQdVnd.divide(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)); //Value with VS
										    			else 
										    				doiSoatData.setPhiXuLyGd(BigDecimal.ZERO);
										    			doiSoatData.setMerchantCity(row.getCell(22)==null ? "" : row.getCell(22).getStringCellValue()); //Value with VS
										    			doiSoatData.setMcc(row.getCell(24)==null ? "" : row.getCell(24).getStringCellValue());
										    			
										    			
										    			if(doisoatDataTemp.size()>0)
										    			{
											    			for (Object[] b : doisoatDataTemp) {
				
										    					doiSoatData.setLoaiTienNguyenTeGd(b[12]==null ? " " : b[12].toString());
										    					
										    					doiSoatData.setTenChuThe(b[1]==null ? " " : b[1].toString());
										    					
										    					doiSoatData.setTenChuTk(b[16]==null ? " " : b[16].toString());
										    						
										    					doiSoatData.setCasa(b[11]==null ? " " : b[11].toString());
										    						
										    					doiSoatData.setStatusCw(b[9]==null ? " " : b[9].toString());
										    					
										    					doiSoatData.setDvpht(b[8]==null ? " " : b[8].toString());
										    						
										    					doiSoatData.setCif(b[14]==null ? " " : b[14].toString());
										    					
										    					doiSoatData.setCrdpgm(b[15]==null ? " " : b[15].toString());
										    					
										    					doiSoatData.setTrace(b[10]==null ? " " : b[10].toString());
										    					
									    						doiSoatData.setStTrichNoKhGd(b[4] != null ? new BigDecimal(b[4].toString().replace(",", "")) : BigDecimal.ZERO);
										    					doiSoatData.setStgdNguyenTeGd(b[3] != null ? new BigDecimal(b[3].toString().replace(",", "")) : BigDecimal.ZERO);
										    					doiSoatData.setPhiRtmGd(b[7]==null ? BigDecimal.ZERO : new BigDecimal(b[7].toString()));
										    					doiSoatData.setPhiIsaGd(b[6]==null ? BigDecimal.ZERO : new BigDecimal(b[6].toString()));
										    					
										    					if(ltgd.equals("IDR")) {
										    						doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().multiply(BigDecimal.valueOf(100)));
										    					}
										    				}
										    			}
										    			else {
										    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
									    					doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
									    					doiSoatData.setPhiRtmGd(BigDecimal.ZERO);
									    					doiSoatData.setPhiIsaGd(BigDecimal.ZERO);
										    			}
										    			
										    			if(maGd.startsWith("06") || maGd.startsWith("25") || maGd.startsWith("27")) {
										    				stGd = stGd.negate();
										    				stQdVnd = stQdVnd.negate();
										    				doiSoatData.setStTrichNoKhGd(doiSoatData.getStTrichNoKhGd().negate());
									    					doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().negate());
										    			}
										    			
									    				doiSoatData.setStGd(stGd);
										    			doiSoatData.setStTqt(BigDecimal.ZERO);
										    			doiSoatData.setStQdVnd(stQdVnd);
										    			
										    			doiSoatData.setInterchange(interchange);
										    			doiSoatData.setStgdNguyenTeChenhLech(stGd.subtract(doiSoatData.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatData.getStgdNguyenTeGd()));
									    				doiSoatData.setStgdChenhLechDoTyGia(stQdVnd.subtract(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd()));
									    				
										    			if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
									    					doiSoatData.setTyGiaTrichNo(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd().divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP));
									    				else
									    					doiSoatData.setTyGiaTrichNo(BigDecimal.ZERO);
									    				doiSoatData.setSoTienGdHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
									    				
									    				if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
									    				{
									    					doiSoatData.setPhiIsaHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiIsaGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
									    				}
									    				else 
									    					doiSoatData.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
									    				
									    				doiSoatData.setVatPhiIsaHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
									    				
									    				if(!doiSoatData.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
									    					doiSoatData.setPhiRtmHoanTraTruyThu(doiSoatData.getStgdNguyenTeChenhLech().multiply(doiSoatData.getPhiRtmGd()).divide(doiSoatData.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
									    				else 
									    					doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
									    				
									    				doiSoatData.setVatPhiRtmHoanTraTruyThu(doiSoatData.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
									    				doiSoatData.setTongPhiVatHoanTraTruyThu(doiSoatData.getPhiIsaHoanTraTruyThu().add(doiSoatData.getVatPhiIsaHoanTraTruyThu()).add(doiSoatData.getPhiRtmHoanTraTruyThu()).add(doiSoatData.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
									    				doiSoatData.setTongHoanTraTruyThu(doiSoatData.getSoTienGdHoanTraTruyThu().add(doiSoatData.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
									    				
									    				DoiSoatData doiSoatDataCheck = null;
									    				if(!((maGd.startsWith("25") && doiSoatData.getStatusCw().equals(" "))
							    						|| (maGd.startsWith("06") && doiSoatData.getStatusCw().equals(" ") && !doiSoatData.getMerchantCity().contains("Visa Direct"))
							    						|| (maGd.startsWith("27") && doiSoatData.getStatusCw().equals(" "))))
									    				{
									    					doiSoatDataCheck = doiSoatDataService.findOneByCardnoAndApvcodeAndMagdAndAndReversalIdAndAdvdate(cardnoVS, apvcode,maGd," ",advDate);
									    				}
									    				
										    			if(doiSoatDataCheck!=null) {
										    				//Accumulated value
										    				doiSoatDataCheck.setStGd(doiSoatDataCheck.getStGd().add(stGd));
										    				doiSoatDataCheck.setStQdVnd(doiSoatDataCheck.getStQdVnd().add(stQdVnd));
										    				doiSoatDataCheck.setStgdNguyenTeChenhLech(doiSoatDataCheck.getStGd().subtract(doiSoatDataCheck.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStgdNguyenTeGd()));
										    				doiSoatDataCheck.setStgdChenhLechDoTyGia(doiSoatDataCheck.getStQdVnd().subtract(doiSoatDataCheck.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStTrichNoKhGd()));
										    				doiSoatDataCheck.setSoTienGdHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getTyGiaTrichNo()).setScale(0, RoundingMode.HALF_UP));
										    				
										    				if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
										    				{
										    					doiSoatDataCheck.setPhiIsaHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiIsaGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
										    				}
										    				else 
										    					doiSoatDataCheck.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatDataCheck.setVatPhiIsaHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
										    				
										    				if(!doiSoatDataCheck.getStgdNguyenTeGd().equals(BigDecimal.ZERO))
										    					doiSoatDataCheck.setPhiRtmHoanTraTruyThu(doiSoatDataCheck.getStgdNguyenTeChenhLech().multiply(doiSoatDataCheck.getPhiRtmGd()).divide(doiSoatDataCheck.getStgdNguyenTeGd(),5,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
										    				else 
										    					doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
										    				
										    				doiSoatDataCheck.setVatPhiRtmHoanTraTruyThu(doiSoatDataCheck.getPhiRtmHoanTraTruyThu().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.HALF_UP));
										    				doiSoatDataCheck.setTongPhiVatHoanTraTruyThu(doiSoatDataCheck.getPhiIsaHoanTraTruyThu().add(doiSoatDataCheck.getVatPhiIsaHoanTraTruyThu()).add(doiSoatDataCheck.getPhiRtmHoanTraTruyThu()).add(doiSoatDataCheck.getVatPhiRtmHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
										    				doiSoatDataCheck.setTongHoanTraTruyThu(doiSoatDataCheck.getSoTienGdHoanTraTruyThu().add(doiSoatDataCheck.getTongPhiVatHoanTraTruyThu()).setScale(0, RoundingMode.HALF_UP));
										    				
										    				doiSoatDataService.create(doiSoatDataCheck);
										    				
										    				//Add new row
										    				doiSoatData.setStGd(BigDecimal.ZERO);
										    				doiSoatData.setStTqt(BigDecimal.ZERO);
										    				doiSoatData.setStQdVnd(BigDecimal.ZERO);
										    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
										    				doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
										    				doiSoatData.setStgdNguyenTeChenhLech(BigDecimal.ZERO);
										    				doiSoatData.setStgdChenhLechDoTyGia(BigDecimal.ZERO);
										    				doiSoatData.setSoTienGdHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setVatPhiIsaHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setVatPhiRtmHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setTongPhiVatHoanTraTruyThu(BigDecimal.ZERO);
										    				doiSoatData.setTongHoanTraTruyThu(BigDecimal.ZERO);
										    				
											    			doiSoatDataService.create(doiSoatData);
										    			} else
										    				doiSoatDataService.create(doiSoatData);
									    			
									    			}
							    					break;
							    					
							    				case "VS":
							    					cardnoVS = row.getCell(8)==null ? "" : row.getCell(8).getStringCellValue();
							    					lttqt = row.getCell(27)==null ? "" : row.getCell(27).getStringCellValue();
							    					loaiTienTQT = cbbLoaiTienTQT.getValue().toString();
							    					System.out.println("Card no: " + cardnoVS + ", lttqt: " + lttqt + ", loaiTienTQT: " + loaiTienTQT);
									    			if((cardnoVS.startsWith("489516") || cardnoVS.startsWith("489517") || cardnoVS.startsWith("489518")) 
									    			&& (loaiTienTQT.equals("All") || loaiTienTQT.equals(lttqt))) { 
									    				id++;
									    				String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
									    				String maGd = row.getCell(0)==null ? "" : row.getCell(0).getStringCellValue();
									    				String ngayGd = "";
									    				if(row.getCell(38).getCellType() == Cell.CELL_TYPE_BLANK) 
									    					ngayGd = row.getCell(38)==null ? "" : "20" + String.format("%04d",Integer.valueOf(row.getCell(38).getStringCellValue()));
									    				else
									    					ngayGd = row.getCell(38).getStringCellValue().isEmpty() ? "" : "20" + String.format("%04d",Integer.valueOf(row.getCell(38).getStringCellValue()));
									    				 
										    			String apvcode = row.getCell(43)==null ? "" : row.getCell(43).getStringCellValue().replaceAll("[\\s|\\u00A0]+", "");
										    			
										    			BigDecimal stGd = row.getCell(12)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(12).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);
										    			BigDecimal stQdVnd = BigDecimal.ZERO;
										    			if(row.getCell(10).getCellType() == Cell.CELL_TYPE_BLANK) 
										    				stQdVnd = BigDecimal.ZERO;
										    			else
										    				stQdVnd = row.getCell(10)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(10).getNumericCellValue()).setScale(2,RoundingMode.HALF_UP);
										    			BigDecimal interchange = row.getCell(28)==null ? BigDecimal.ZERO : new BigDecimal(row.getCell(28).getStringCellValue().replace(",", ""));
										    			System.out.println("STT: " + id + ", Rownum: " + row.getRowNum() + ", cardno: "+ cardnoVS + ", apvcode: " + apvcode + ", stGd: " + stGd);
										    			String ltgd = row.getCell(13)==null ? "" : row.getCell(13).getStringCellValue();
										    			String issuerCharge = row.getCell(49)==null ? "" : row.getCell(49).getStringCellValue();
										    			String ngayFileIncoming = filename.substring(filename.lastIndexOf("INCTF.")+6, filename.lastIndexOf("INCTF.")+20);
										    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
										    			Date ngayFileIncomingConvers = sdf.parse(ngayFileIncoming);
										    			sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
										    			ngayFileIncoming = sdf.format(ngayFileIncomingConvers);
										    			
										    			String pan = doiSoatDataService.convertPanByCardno(cardnoVS);
										    			
										    			List<Object[]> doisoatDataTemp = doiSoatDataService.findDoiSoatInfoByPanAndApvCode(pan,apvcode,ngayGd);
										    			
										    			DoiSoatData doiSoatData = new DoiSoatData();
										    			doiSoatData.setId(timeConverter.getCurrentTime() + String.format("%07d", id));
										    			doiSoatData.setCreTms(new BigDecimal(timeConverter.getCurrentTime()));
										    			doiSoatData.setUsrId(sUserId);
										    			doiSoatData.setNgayAdv(advDate);
										    			doiSoatData.setSoThe(cardnoVS);
										    			doiSoatData.setMaGd(maGd);
										    			doiSoatData.setNgayGd(ngayGd);
										    			doiSoatData.setNgayFileIncoming(ngayFileIncoming);
										    			doiSoatData.setLtgd(ltgd);
										    			doiSoatData.setLttqt(lttqt);
										    			doiSoatData.setMaCapPhep(apvcode);
										    			doiSoatData.setDvcnt(row.getCell(21)==null ? "" : row.getCell(21).getStringCellValue());
										    			doiSoatData.setReversalInd(" ");
										    			doiSoatData.setPan(pan);
										    			doiSoatData.setIssuerCharge(issuerCharge); //Value with VS
										    			if(issuerCharge.equals("S"))
										    				doiSoatData.setPhiXuLyGd(stQdVnd.divide(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)); //Value with VS
										    			else 
										    				doiSoatData.setPhiXuLyGd(BigDecimal.ZERO);
										    			doiSoatData.setMerchantCity(row.getCell(22)==null ? "" : row.getCell(22).getStringCellValue()); //Value with VS
										    			doiSoatData.setMcc(row.getCell(24)==null ? "" : row.getCell(24).getStringCellValue());
										    			
										    			
										    			if(maGd.startsWith("06") || maGd.startsWith("25")) {
											    			if(doisoatDataTemp.size()>0)
											    			{
												    			for (Object[] b : doisoatDataTemp) {
					
											    					doiSoatData.setLoaiTienNguyenTeGd(b[12]==null ? " " : b[12].toString());
											    					
											    					doiSoatData.setTenChuThe(b[1]==null ? " " : b[1].toString());
											    					
											    					doiSoatData.setTenChuTk(b[16]==null ? " " : b[16].toString());
											    						
											    					doiSoatData.setCasa(b[11]==null ? " " : b[11].toString());
											    						
											    					doiSoatData.setDvpht(b[8]==null ? " " : b[8].toString());
											    						
											    					doiSoatData.setCif(b[14]==null ? " " : b[14].toString());
											    					
											    					doiSoatData.setCrdpgm(b[15]==null ? " " : b[15].toString());
											    					
										    						doiSoatData.setStTrichNoKhGd(b[20] != null ? new BigDecimal(b[20].toString().replace(",", "")) : BigDecimal.ZERO);
											    					doiSoatData.setStgdNguyenTeGd(b[19] != null ? new BigDecimal(b[19].toString().replace(",", "")) : BigDecimal.ZERO);
											    					doiSoatData.setPhiRtmGd(b[7]==null ? BigDecimal.ZERO : new BigDecimal(b[7].toString()));
											    					doiSoatData.setPhiIsaGd(b[6]==null ? BigDecimal.ZERO : new BigDecimal(b[6].toString()));
											    					doiSoatData.setVatPhiRtmGd(b[18]==null ? BigDecimal.ZERO : new BigDecimal(b[18].toString()));
											    					doiSoatData.setVatPhiIsaGd(b[17]==null ? BigDecimal.ZERO : new BigDecimal(b[17].toString()));
											    					
											    					if(ltgd.equals("IDR")) {
											    						doiSoatData.setStgdNguyenTeGd(doiSoatData.getStgdNguyenTeGd().multiply(BigDecimal.valueOf(100)));
											    					}
											    				}
											    			}
											    			else {
											    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
										    					doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
										    					doiSoatData.setPhiRtmGd(BigDecimal.ZERO);
										    					doiSoatData.setPhiIsaGd(BigDecimal.ZERO);
											    			}
										    			
										    				stGd = stGd.negate();
										    				stQdVnd = stQdVnd.negate();
										    			}
										    			
									    				doiSoatData.setStGd(stGd);
										    			doiSoatData.setStTqt(BigDecimal.ZERO);
										    			doiSoatData.setStQdVnd(stQdVnd);
										    			
										    			doiSoatData.setInterchange(interchange);
										    			doiSoatData.setStgdNguyenTeChenhLech(stGd.add(doiSoatData.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatData.getStgdNguyenTeGd()));
									    				doiSoatData.setStgdChenhLechDoTyGia(stQdVnd.add(doiSoatData.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatData.getStTrichNoKhGd()));
									    				
									    				DoiSoatData doiSoatDataCheck = null;
									    				if(!((maGd.startsWith("25") && doiSoatData.getStatusCw().equals(" "))
									    						|| (maGd.startsWith("06") && doiSoatData.getStatusCw().equals(" ") && !doiSoatData.getMerchantCity().contains("Visa Direct"))
									    						|| (maGd.startsWith("27") && doiSoatData.getStatusCw().equals(" "))))
									    				{
									    					doiSoatDataCheck = doiSoatDataService.findOneByCardnoAndApvcodeAndMagdAndAndReversalIdAndAdvdate(cardnoVS, apvcode,maGd," ",advDate);
									    				}
									    				if(doiSoatDataCheck!=null) {
										    				//Accumulated value
										    				doiSoatDataCheck.setStGd(doiSoatDataCheck.getStGd().add(stGd));
										    				doiSoatDataCheck.setStQdVnd(doiSoatDataCheck.getStQdVnd().add(stQdVnd));
										    				doiSoatDataCheck.setStgdNguyenTeChenhLech(doiSoatDataCheck.getStGd().add(doiSoatDataCheck.getStgdNguyenTeGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStgdNguyenTeGd()));
										    				doiSoatDataCheck.setStgdChenhLechDoTyGia(doiSoatDataCheck.getStQdVnd().add(doiSoatDataCheck.getStTrichNoKhGd()==null?BigDecimal.ZERO:doiSoatDataCheck.getStTrichNoKhGd()));
										    				
										    				doiSoatDataService.create(doiSoatDataCheck);
										    				
										    				//Add new row
										    				doiSoatData.setStGd(BigDecimal.ZERO);
										    				doiSoatData.setStTqt(BigDecimal.ZERO);
										    				doiSoatData.setStQdVnd(BigDecimal.ZERO);
										    				doiSoatData.setStTrichNoKhGd(BigDecimal.ZERO);
										    				doiSoatData.setStgdNguyenTeGd(BigDecimal.ZERO);
										    				doiSoatData.setStgdNguyenTeChenhLech(BigDecimal.ZERO);
										    				doiSoatData.setStgdChenhLechDoTyGia(BigDecimal.ZERO);
										    				
											    			doiSoatDataService.create(doiSoatData);
										    			} else
										    				doiSoatDataService.create(doiSoatData);
									    			
									    			}
							    					break;
							    					
							    			}
							    			
							    			
							    			
							    			
							    		}
									} 
							    	workbook.close();
						            is.close();
									
								} catch (Exception e) {
									// TODO: handle exception
//									LOGGER.error(e.toString());
									e.printStackTrace();
									LOGGER.error("Error: " +  e.getMessage());
								}
								
								String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
								String loaithe = cbbCardType == null ? "All" : cbbCardType.getValue().toString();
								grid.dataSource = getData(loaithe, cbbLoaiTienTQT.getValue().toString(),advDate,new PageRequest(FIRST_OF_PAGE, SIZE_OF_PAGE, Sort.Direction.ASC, "id"));
								grid.initGrid("All");

								grid.refreshData();
								confirmDialog.close();
					        	
					        });
					        
							//-----------------
							VerticalLayout layoutBtn = new VerticalLayout();
				            layoutBtn.addComponents(bYes);
				            layoutBtn.setComponentAlignment(bYes, Alignment.BOTTOM_CENTER);
				            content.addComponent(layoutBtn);
				            
				            confirmDialog.setContent(content);

				            getUI().addWindow(confirmDialog);
				            
				            // Center it in the browser window
				            confirmDialog.center();
				            confirmDialog.setResizable(false);
			        	} else
			        		outputFile = null;
			            
			        } catch (IOException e) {
			            e.printStackTrace();
			            LOGGER.error("Error: " + e.getMessage());
			        }
			        
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					LOGGER.error(e.toString());
				}
				return outputFile;
				
			}
			
			
		});
		chooseFile.setButtonCaption(IMPORT);
		chooseFile.addStyleName("myCustomUpload");
		
		btView.addClickListener(event -> {
			if(!checkValidatorView()) {
				return;
			}
			String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
			String loaithe = cbbCardType == null ? "All" : cbbCardType.getValue().toString();
			grid.dataSource = getData(loaithe, cbbLoaiTienTQT.getValue().toString(),advDate,new PageRequest(FIRST_OF_PAGE, SIZE_OF_PAGE, Sort.Direction.ASC, "id"));
			grid.initGrid("All");

			grid.refreshData();
		});
		
		formLayout1a.addComponent(lbFromIncomingDate);
		formLayout1a.addComponent(dfFromIncomingDate);
		formLayout1a.addComponent(lbToIncomingDate);
		formLayout1a.addComponent(dfToIncomingDate);
		formLayout1a.addComponent(btImport);
		formLayout1b.addComponent(optgrpIncomingFile);
		
		formLayout1st.addComponent(new Label("File incoming"));
		formLayout1st.addComponent(chooseFile);
		formLayout2nd.addComponent(lbAdvDate);
		formLayout2nd.addComponent(dfAdvDate);
//		formLayout2nd.addComponent(lbtoDate);
//		formLayout2nd.addComponent(dftoDate);
		formLayout2nd.addComponent(lbCardType);
		formLayout2nd.addComponent(cbbCardType);
		formLayout2nd.addComponent(lbLoaiTienTQT);
		formLayout2nd.addComponent(cbbLoaiTienTQT);
		formLayout3rd.addComponent(btView);
		formLayout3rd.addComponent(btDoiChieu);
		formLayout3rd.addComponent(cbbLoaiGD);
		
		formLayout1a.setComponentAlignment(lbFromIncomingDate, Alignment.MIDDLE_LEFT);
		formLayout1a.setComponentAlignment(dfFromIncomingDate, Alignment.MIDDLE_LEFT);
		formLayout1a.setComponentAlignment(lbToIncomingDate, Alignment.MIDDLE_LEFT);
		formLayout1a.setComponentAlignment(dfToIncomingDate, Alignment.MIDDLE_LEFT);
		formLayout1a.setComponentAlignment(btImport, Alignment.MIDDLE_LEFT);
		formLayout1b.setComponentAlignment(optgrpIncomingFile, Alignment.MIDDLE_LEFT);
		
		formLayout1st.setComponentAlignment(chooseFile, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(lbAdvDate, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(dfAdvDate, Alignment.MIDDLE_LEFT);
//		formLayout2nd.setComponentAlignment(lbtoDate, Alignment.MIDDLE_LEFT);
//		formLayout2nd.setComponentAlignment(dftoDate, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(lbCardType, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(cbbCardType, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(lbLoaiTienTQT, Alignment.MIDDLE_LEFT);
		formLayout2nd.setComponentAlignment(cbbLoaiTienTQT, Alignment.MIDDLE_LEFT);
		formLayout3rd.setComponentAlignment(btView, Alignment.MIDDLE_CENTER);
		formLayout3rd.setComponentAlignment(btDoiChieu, Alignment.MIDDLE_CENTER);
		formLayout3rd.setComponentAlignment(cbbLoaiGD, Alignment.MIDDLE_CENTER);
		
		formLayout.addComponent(formLayout2nd);
		formLayout.addComponent(formLayout1a);
		formLayout.addComponent(formLayout1b);
		formLayout.addComponent(formLayout1st);
		formLayout.addComponent(formLayout3rd);
		formLayout.setComponentAlignment(formLayout1a, Alignment.MIDDLE_LEFT);
		formLayout.setComponentAlignment(formLayout1b, Alignment.MIDDLE_LEFT);
		formLayout.setComponentAlignment(formLayout1st, Alignment.MIDDLE_LEFT);
		formLayout.setComponentAlignment(formLayout2nd, Alignment.MIDDLE_LEFT);
		formLayout.setComponentAlignment(formLayout3rd, Alignment.MIDDLE_CENTER);
		
		form.addComponent(formLayout);
		mainLayout.addComponent(form);
		mainLayout.setSpacing(true);
		
		grid = new DataGridDoiSoatTQTComponent();
//		grid.initGrid("All");
		mainLayout.addComponent(grid);

//		generatePagingLayout().setVisible(false);
//		pagingLayout = generatePagingLayout();
//		pagingLayout.setSpacing(true);
//		
//		mainLayout.addComponent(pagingLayout);
//		mainLayout.setComponentAlignment(pagingLayout, Alignment.BOTTOM_RIGHT);
		
		setCompositionRoot(mainLayout);
	}
	

//	private HorizontalLayout generatePagingLayout() {
//		final Button btPaging = new Button();
//		btPaging.setCaption(reloadLabelPaging());
//		btPaging.setEnabled(false);
//
//		final Button btPreviousPage = new Button("Trang trước");
//		btPreviousPage.setIcon(FontAwesome.ANGLE_LEFT);
//		btPreviousPage.setEnabled(true);
//
//		final Button btNextPage = new Button("Trang sau");
//		btNextPage.setStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
//		btNextPage.setIcon(FontAwesome.ANGLE_RIGHT);
//		btNextPage.setEnabled(true);
//		
//		final HorizontalLayout pagingLayout = new HorizontalLayout();
//		pagingLayout.setSizeUndefined();
//		pagingLayout.setSpacing(true);
//		pagingLayout.addComponent(btPaging);
//		pagingLayout.addComponent(btPreviousPage);
//		pagingLayout.addComponent(btNextPage);
//		pagingLayout.setDefaultComponentAlignment(Alignment.BOTTOM_RIGHT);
//		
// 		return pagingLayout;
//	}

	private Page<DoiSoatData> getData(String loaithe, String lttqt, String ngayAdv, Pageable page) {
		doiSoatDataList = doiSoatDataService.findAllByLoaiTheAndLttqtAndNgayAdv(loaithe, lttqt, ngayAdv);
		result = new PageImpl<>(doiSoatDataList);
		return result;
	}
	
//	private String reloadLabelPaging() {
//		final StringBuilder sNumberOfElements = new StringBuilder();
//		String sTotalElements = null;
//		String sLabelPaging = "";
//		return sLabelPaging;
//	}
	
	@Override
	public void eventReload() {
	}
	
	private Button.ClickListener eventClickBTDoiChieu() {
		return event -> {
			try {
				String advDate = timeConverter.convertDatetime(dfAdvDate.getValue());
				getUI().addWindow(createWindowComponentDoiChieu("Đối chiếu thanh quyết toán", new DoiChieuThanhQuyetToanForm(doiSoatDataList, cbbLoaiTienTQT.getValue().toString(),cbbLoaiGD.getValue().toString(),advDate,cbbCardType.getValue().toString(),this::closeWindow)));
			} catch (Exception e) {
				Notification.show(ERROR_MESSAGE, Type.ERROR_MESSAGE);
				LOGGER.error("DoiChieuClickListener - " + e.getMessage());
				e.printStackTrace();
			
			}
		};
	}
	
	private Window createWindowComponentDoiChieu(final String caption, final Component comp) {
		window.setCaption(caption);
		window.setContent(comp);
		window.setSizeFull();
		window.setWidth(95, Unit.PERCENTAGE);
		return window;
	}
	
	private void closeWindow() {
		getUI().removeWindow(window);
	}
	
	public boolean checkValidatorImport() {
		try {
			dfAdvDate.validate();
			return true;
		} catch (InvalidValueException ex) {
			dfAdvDate.setValidationVisible(true);
		}
		return false;
	}

	public boolean checkValidatorView() {
		try {
			dfAdvDate.validate();
			cbbCardType.validate();
			cbbLoaiTienTQT.validate();
			return true;
		} catch (InvalidValueException ex) {
			dfAdvDate.setValidationVisible(true);
			cbbCardType.setValidationVisible(true);
			cbbLoaiTienTQT.setValidationVisible(true);
		}
		return false;
	}

	
	private void showListIncomingFileName(DateField dfFromIncomingDate,DateField dfToIncomingDate) {
		optgrpIncomingFile.removeAllItems();
		String fromIncomingDate = timeConverter.convertDatetime(dfFromIncomingDate.getValue());
		String toIncomingDate = timeConverter.convertDatetime(dfToIncomingDate.getValue());
		List<String> listMCIncomingFileName = doiSoatDataService.findMCIncomingFileNameByDate(fromIncomingDate,toIncomingDate);
		for(String item : listMCIncomingFileName) {
			optgrpIncomingFile.addItem(item);
			String itemCaption = item.substring(item.lastIndexOf("TT112T0.")+8, item.lastIndexOf("TT112T0.")+27);
			optgrpIncomingFile.setItemCaption(item, itemCaption); 
		}
	}
}
